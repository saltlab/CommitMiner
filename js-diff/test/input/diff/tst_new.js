/**
 * Copyright 2013 the PM2 project authors. All rights reserved.
 * Use of this source code is governed by a license that
 * can be found in the LICENSE file.
 */

var CLI                  = module.exports = {};

var commander            = require('commander');
var fs                   = require('fs');
var path                 = require('path');
var async                = require('async');
var debug                = require('debug')('pm2:monit');
var util                 = require('util');
var chalk                = require('chalk');
var exec                 = require('child_process').exec;
var p                    = path;

var cst                  = require('../constants.js');

var Satan                = require('./Satan');
var Common               = require('./Common');

var InteractorDaemonizer = require('./Interactor/InteractorDaemonizer');
var Config               = require('./tools/Config');
var Utility              = require('./Utility.js');

var Modularizer          = require('./Modularizer.js');
var Configuration        = require('../lib/Configuration.js');

var UX        = require('./CLI/CliUx');
var Log       = require('./CLI/Log');
var CLIDeploy = require('./CLI/Deploy.js');

/**
 * Get version of the daemonized PM2
 * @method getVersion
 * @callback cb
 */
CLI.getVersion = function(cb) {
  Satan.executeRemote('getVersion', {}, function(err) {
    return cb ? cb.apply(null, arguments) : Common.exitCli(cst.SUCCESS_EXIT);
  });
};

/**
 * Description
 * @method killDaemon
 * @param {} cb
 * @return
 */
CLI.killDaemon = CLI.kill = function(cb) {
  var semver = require('semver');
  Common.printOut(cst.PREFIX_MSG + 'Stopping PM2...');

  Satan.executeRemote('notifyKillPM2', {}, function() {});

  CLI.getVersion(function(err, data) {
    if (!err && semver.lt(data, '1.1.0')) {
      // Disable action command output if upgrading from < 1.1.0 PM2
      // This is in order to avoid duplicated output
      process.env.PM2_SILENT = true;
      console.log(cst.PREFIX_MSG + 'Killing processes...');
    }

    CLI.killAllModules(function() {
      CLI._operate('deleteProcessId', 'all', function(err, list) {
        Common.printOut(cst.PREFIX_MSG + 'All processes have been stopped and deleted');
        process.env.PM2_SILENT = false;

        InteractorDaemonizer.killDaemon(function(err, data) {
          Satan.killDaemon(function(err, res) {
            if (err) Common.printError(err);
            Common.printOut(cst.PREFIX_MSG + 'PM2 stopped');
            return cb ? cb(err, res) : Common.exitCli(cst.SUCCESS_EXIT);
          });
        });
      });
    });

  });
};

CLI.killAllModules = function(cb) {
  Common.getAllModulesId(function(err, modules_id) {
    async.forEachLimit(modules_id, 1, function(id, next) {
      CLI._operate('deleteProcessId', id, next);
    }, function() {
      return cb ? cb() : false;
    });
  });
};

/**
 * This methods is used for stop, delete and restart
 * Module cannot be stopped or deleted but can be restarted
 */
CLI._operate = function(action_name, process_name, envs, cb) {
  var ret = ["one", "two", "three"];

  // Make sure all options exist

  if (!envs)
    envs = {};

  if (typeof(envs) == 'function'){
    cb = envs;
    envs = {};
  }

  if (!process.env.PM2_JSON_PROCESSING)
    envs = CLI._handleAttributeUpdate(envs);

  /**
   * Operate action on specific process id
   */
  function processIds(ids, cb) {
    Common.printOut(cst.PREFIX_MSG + 'Applying action %s on app [%s](ids: %s)', action_name, process_name, ids);

    async.eachLimit(ids, cst.CONCURRENT_ACTIONS, function(id, next) {
      var opts = id;

      if (action_name == 'restartProcessId' ||
          action_name == 'reloadProcessId' ||
          action_name == 'softReloadProcessId') {
        var new_env = {};

        if (!opts.skipEnv) {
          new_env = util._extend({}, process.env);
          Object.keys(envs).forEach(function(k) {
            new_env[k] = envs[k];
          });
        } else {
          new_env = envs;
        }

        opts = {
          id  : id,
          env : new_env
        };
      }

      Satan.executeRemote(action_name, opts, function(err, res) {
        if (err) {
          Common.printError(cst.PREFIX_MSG_ERR + 'Process %s not found', id);
          return next('Process not found');
        }

        if (action_name == 'restartProcessId') {
          Satan.notifyGod('restart', id);
        } else if (action_name == 'deleteProcessId') {
          Satan.notifyGod('delete', id);
        } else if (action_name == 'stopProcessId') {
          Satan.notifyGod('stop', id);
        } else if (action_name == 'reloadProcessId') {
          Satan.notifyGod('reload', id);
        } else if (action_name == 'softReloadProcessId') {
          Satan.notifyGod('graceful reload', id);
        }

        if (!Array.isArray(res))
          res = [res];

        // Filter return
        res.forEach(function(proc) {
          Common.printOut(cst.PREFIX_MSG + '[%s](%d) \u2713', proc.pm2_env ? proc.pm2_env.name : process_name, id);

          ret.push({
            name         : proc.pm2_env.name,
            pm_id        : proc.pm2_env.pm_id,
            status       : proc.pm2_env.status,
            restart_time : proc.pm2_env.restart_time,
            pm2_env : {
              name         : proc.pm2_env.name,
              pm_id        : proc.pm2_env.pm_id,
              status       : proc.pm2_env.status,
              restart_time : proc.pm2_env.restart_time,
              env          : proc.pm2_env.env
            }
          });
        });

        return next();
      });
    }, function(err) {
      if (err) return cb ? cb(new Error(err)) : Common.exitCli(cst.ERROR_EXIT);
      return cb ? cb(null, ret) : speedList();
    });
  }

  if (process_name == 'all') {
    Common.getAllProcessId(function(err, ids) {
      if (err) {
        Common.printError(err);
        return cb ? cb({msg:err}) : Common.exitCli(cst.ERROR_EXIT);
      }
      if (!ids || ids.length === 0) {
        Common.printError(cst.PREFIX_MSG_WARNING + 'No process found');
        return cb ? cb({ success : false, msg : 'process name not found'}) : Common.exitCli(cst.ERROR_EXIT);
      }

      return processIds(ids, cb);
    });
  }
  else if (isNaN(process_name)) {

    /**
     * We can not stop or delete a module but we can restart it
     * to refresh configuration variable
     */
    var allow_module_restart = action_name == 'restartProcessId' ? true : false;

    Common.getProcessIdByName(process_name, allow_module_restart, function(err, ids) {
      if (err) {
        Common.printError(err);
        return cb ? cb({msg:err}) : Common.exitCli(cst.ERROR_EXIT);
      }
      if (!ids || ids.length === 0) {
        Common.printError(cst.PREFIX_MSG_ERR + 'Process %s not found', process_name);
        return cb ? cb({ success : false, msg : 'process name not found'}) : Common.exitCli(cst.ERROR_EXIT);
      }

      /**
       * Determine if the process to restart is a module
       * if yes load configuration variables and merge with the current environment
       */
      var additional_env = Modularizer.getAdditionalConf(process_name);
      util._extend(envs, additional_env);

      return processIds(ids, cb);
    });
  } else {
    // Check if application name as number is an app name
    Common.getProcessIdByName(process_name, function(err, ids) {
      if (ids.length > 0)
        return processIds(ids, cb);
      // Else operate on pm id
      return processIds([process_name], cb);
    });
  }
};
