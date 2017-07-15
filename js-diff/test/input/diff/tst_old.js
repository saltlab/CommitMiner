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
 * Initialize all folders depending on cst.PM2_ROOT_PATH
 */
CLI.pm2Init = function() {
  if (!fs.existsSync(cst.PM2_ROOT_PATH)) {
    fs.mkdirSync(cst.PM2_ROOT_PATH);
    fs.mkdirSync(cst.DEFAULT_LOG_PATH);
    fs.mkdirSync(cst.DEFAULT_PID_PATH);
  }

  if (!fs.existsSync(cst.PM2_CONF_FILE)) {
    fs
      .createReadStream(path.join(__dirname, cst.SAMPLE_CONF_FILE))
      .pipe(fs.createWriteStream(cst.PM2_CONF_FILE));
  }

  if (cst.PM2_HOME && !fs.existsSync(cst.PM2_HOME)) {
    try {
      fs.mkdirSync(cst.PM2_HOME);
      fs.mkdirSync(cst.DEFAULT_LOG_PATH);
      fs.mkdirSync(cst.DEFAULT_PID_PATH);
    } catch(e) {
      debug(e.stack || e);
    }
  }

  if (!fs.existsSync(cst.PM2_MODULE_CONF_FILE)) {
    try {
      fs.writeFileSync(cst.PM2_MODULE_CONF_FILE, "{}");
    } catch (e) {
      console.error(e.stack || e);
    }
  }

  if (!fs.existsSync(p.join(cst.PM2_HOME, 'touch'))) {
    var dt = fs.readFileSync(path.join(__dirname, cst.KEYMETRICS_BANNER));
    console.log(dt.toString());
    try {
      fs.writeFileSync(p.join(cst.PM2_HOME, 'touch'), Date.now());
    } catch(e) {
      debug(e.stack || e);
    }
  }

  if (process.stdout._handle && process.stdout._handle.setBlocking)
    process.stdout._handle.setBlocking(true);
};

/**
 * API Methods
 */
CLI.connect = Satan.start;

CLI.launchBus = Satan.launchBus;

CLI.disconnectBus = Satan.disconnectBus;

CLI.disconnect = function(cb) {
  if (!cb) cb = function() {};
  Satan.disconnectRPC(cb);
};

/**
 * Method to START / RESTART a script
 * @method startFile
 * @param {string} script script name (will be resolved according to location)
 * @return
 */
CLI._startScript = function(script, opts, cb) {
  if (typeof opts == "function") {
    cb = opts;
    opts = {};
  }

  var conf = Config.transCMDToConf(opts);
  var appConf = {};

  if (!!opts.executeCommand)
    conf.exec_mode = 'fork';
  else if (opts.instances !== undefined)
    conf.exec_mode = 'cluster';
  else
    conf.exec_mode = 'fork';

  if (typeof conf.name == 'function'){
    delete conf.name;
  }

  delete conf.args;

  var argsIndex;

  if (opts.rawArgs && (argsIndex = opts.rawArgs.indexOf('--')) >= 0) {
    conf.args = opts.rawArgs.slice(argsIndex + 1);
  }
  else if (opts.scriptArgs) {
    conf.args = opts.scriptArgs;
  }

  conf.script = script;

  if ((appConf = verifyConfs(conf)) === null)
    return Common.exitCli(cst.ERROR_EXIT);

  conf = appConf[0];

  /**
   * If -w option, write configuration to configuration.json file
   */
  if (appConf.write) {
    var dst_path = path.join(process.env.PWD, conf.name + '-pm2.json');
    Common.printOut(cst.PREFIX_MSG + 'Writing configuration to', chalk.blue(dst_path));
    // pretty JSON
    try {
      fs.writeFileSync(dst_path, JSON.stringify(conf, null, 2));
    } catch (e) {
      console.error(e.stack || e);
    }
  }

  /**
   * If start <app_name> start/restart application
   */
  function restartExistingProcessName(cb) {
    if (!isNaN(script) ||
        (typeof script === 'string' && script.indexOf('/') != -1) ||
        (typeof script === 'string' && path.extname(script) !== ''))
      return cb(null);

    if (script !== 'all') {
      Common.getProcessIdByName(script, function(err, ids) {
        if (err && cb) return cb(err);
        if (ids.length > 0) {
          CLI._restart(script, opts, function(err, list) {
            if (err) return cb(err);
            Common.printOut(cst.PREFIX_MSG + 'Process successfully started');
            return cb(true, list);
          });
        }
        else return cb(null);
      });
    }
    else {
      CLI._restart('all', function(err, list) {
        if (err) return cb(err);
        Common.printOut(cst.PREFIX_MSG + 'Process successfully started');
        return cb(true, list);
      });
    }
  }

  function restartExistingProcessId(cb) {
    if (isNaN(script)) return cb(null);

    CLI._restart(script, opts, function(err, list) {
      if (err) return cb(err);
      Common.printOut(cst.PREFIX_MSG + 'Process successfully started');
      return cb(true, list);
    });
  }

  /**
   * Restart a process with the same full path
   * Or start it
   */
  function restartExistingProcessPath(cb) {
    Satan.executeRemote('findByFullPath', path.resolve(process.cwd(), script), function(err, exec) {
      if (err) return cb ? cb(new Error(err)) : Common.exitCli(cst.ERROR_EXIT);

      if (exec && (exec[0].pm2_env.status == cst.STOPPED_STATUS ||
                   exec[0].pm2_env.status == cst.STOPPING_STATUS ||
                   exec[0].pm2_env.status == cst.ERRORED_STATUS)) {
        // Restart process if stopped
        var app_name = exec[0].pm2_env.name;

        CLI._restart(app_name, opts, function(err, list) {
          if (err) return cb ? cb(new Error(err)) : Common.exitCli(cst.ERROR_EXIT);

          Common.printOut(cst.PREFIX_MSG + 'Process successfully started');
          return cb(true, list);
        });
        return false;
      }
      else if (exec && !opts.force) {
        Common.printError(cst.PREFIX_MSG_ERR + 'Script already launched, add -f option to force re-execution');
        return cb(new Error('Script already launched'));
      }

      var resolved_paths = null;

      try {
        resolved_paths = resolvePaths(conf);
      } catch(e) {
        Common.printError(e);
        return cb(e);
      }

      Common.printOut(cst.PREFIX_MSG + 'Starting %s in %s (%d instance' + (resolved_paths.instances > 1 ? 's' : '') + ')',
                      script, resolved_paths.exec_mode, resolved_paths.instances);

      if (!resolved_paths.env) resolved_paths.env = {};
      var additional_env = Modularizer.getAdditionalConf(resolved_paths.name);
      util._extend(resolved_paths.env, additional_env);

      Satan.executeRemote('prepare', resolved_paths, function(err, data) {
        if (err) {
          Common.printError(cst.PREFIX_MSG_ERR + 'Error while launching application', err.stack || err);
          return cb({msg : err});
        }

        Common.printOut(cst.PREFIX_MSG + 'Done.');
        return cb(true, data);
      });
      return false;
    });
  }

  async.series([
    restartExistingProcessName,
    restartExistingProcessId,
    restartExistingProcessPath
  ], function(err, data) {

    if (err instanceof Error) {
      return cb ? cb(err) : Common.exitCli(cst.ERROR_EXIT);
    }

    var ret = {};
    data.forEach(function(_dt) {
      if (_dt !== undefined)
        ret = _dt;
    });

    return cb ? cb(null, ret) : speedList();
  });
};

/**
 * Method to start/restart/reload processes from a JSON file
 * It will start app not started
 * Can receive only option to skip applications
 */
CLI._startJson = function(file, opts, action, pipe, cb) {
  var config     = {};
  var appConf    = {};
  var deployConf = {};
  var apps_info  = [];

  if (typeof(cb) === 'undefined' && typeof(pipe) === 'function')
    cb = pipe;

  if (typeof(file) === 'object')
    config = file;
  else if (pipe == 'pipe')
    config = Utility.parseConfig(file, 'pipe');
  else {
    var data = null;

    try {
      data = fs.readFileSync(file);
    } catch(e) {
      Common.printError(cst.PREFIX_MSG_ERR + 'File ' + file +' not found');
      return cb ? cb(e) : Common.exitCli(cst.ERROR_EXIT);
    }

    try {
      config = Utility.parseConfig(data, file);
    } catch(e) {
      Common.printError(cst.PREFIX_MSG_ERR + 'File ' + file + ' malformated');
      console.error(e);
      return cb ? cb(e) : Common.exitCli(cst.ERROR_EXIT);
    }
  }

  if (config.deploy)
    deployConf = config.deploy;

  if (config.apps)
    appConf = config.apps;
  else
    appConf = config;

  if (!Array.isArray(appConf))
    appConf = [appConf]; //convert to array

  if ((appConf = verifyConfs(appConf)) === null)
    return cb ? cb({success:false}) : Common.exitCli(cst.ERROR_EXIT);

  process.env.PM2_JSON_PROCESSING = true;

  // Get App list
  var apps_name = [];
  var proc_list = {};

  appConf.forEach(function(app) {
    if (opts.only && opts.only != app.name) return false;
    apps_name.push(app.name);
  });

  Satan.executeRemote('getMonitorData', {}, function(err, raw_proc_list) {
    if (err) {
      Common.printError(err);
      return cb ? cb({msg:err}) : Common.exitCli(cst.ERROR_EXIT);
    }

    /**
     * Uniquify in memory process list
     */
    raw_proc_list.forEach(function(proc) {
      proc_list[proc.name] = proc;
    });

    /**
     * Auto detect application already started
     * and act on them depending on action
     */
    async.eachLimit(Object.keys(proc_list), cst.CONCURRENT_ACTIONS, function(proc_name, next) {
      // Skip app name (--only option)
      if (apps_name.indexOf(proc_name) == -1)
        return next();

      if (!(action == 'reloadProcessId' ||
            action == 'softReloadProcessId' ||
            action == 'restartProcessId'))
        throw new Error('Wrong action called');


      // Get `env` from appConf by name
      async.filter(appConf, function(app, callback){
        callback(app.name == proc_name);
      }, function(apps){
        var envs = apps.map(function(app){
          // Binds env_diff to env and returns it.
          return mergeEnvironmentVariables(app, opts.env, deployConf);
        });
        // Assigns own enumerable properties of all
        // Notice: if people use the same name in different apps,
        //         duplicated envs will be overrode by the last one
        var env = envs.reduce(function(e1, e2){
          return util._extend(e1, e2);
        });

        // Pass `env` option
        CLI._operate(action, proc_name, env, function(err, ret) {
          if (err) Common.printError(err);

          // For return
          apps_info = apps_info.concat(ret);

          Satan.notifyGod(action, proc_name);
          // And Remove from array to spy
          apps_name.splice(apps_name.indexOf(proc_name), 1);
          return next();
        });
      });

    }, function(err) {
      if (err) return cb ? cb(new Error(err)) : Common.exitCli(cst.ERROR_EXIT);
      if (apps_name.length > 0 && action != 'start')
        Common.printOut(cst.PREFIX_MSG_WARNING + 'Applications %s not running, starting...', apps_name.join(', '));
      // Start missing apps
      return startApps(apps_name, function(err, apps) {
        apps_info = apps_info.concat(apps);
        return cb ? cb(err, apps_info) : speedList(err ? 1 : 0);
      });
    });
    return false;
  });

  function startApps(app_name_to_start, cb) {
    var apps_to_start = [];

    appConf.forEach(function(app, i) {
      if (app_name_to_start.indexOf(app.name) != -1) {
        apps_to_start.push(appConf[i]);
      }
    });

    async.eachLimit(apps_to_start, cst.CONCURRENT_ACTIONS, function(app, next) {

      if (opts.cwd)
        app.cwd = opts.cwd;
      if (opts.force_name)
        app.name = opts.force_name;
      if (opts.started_as_module)
        app.pmx_module = true;

      var resolved_paths = null;

      try {
        resolved_paths = resolvePaths(app);
      } catch (e) {
        Common.printError(e);
        return cb ? cb({msg : e.message || e}) : Common.exitCli(cst.ERROR_EXIT);
      }

      if (!resolved_paths.env) resolved_paths.env = {};
      var additional_env = Modularizer.getAdditionalConf(resolved_paths.name);
      util._extend(resolved_paths.env, additional_env);

      mergeEnvironmentVariables(app, opts.env, deployConf);

      Satan.executeRemote('prepare', resolved_paths, function(err, data) {
        if (err) {
          Common.printError(cst.PREFIX_MSG + 'Process failed to launch', err);
          return next();
        }

        Common.printOut(cst.PREFIX_MSG + 'App [%s] launched (%d instances)', data[0].pm2_env.name, data.length);
        apps_info = apps_info.concat(data);
        next();
      });

    }, function(err) {
      return cb ? cb(err || null, apps_info) : speedList();
    });
    return false;
  }
};

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
  Common.printOut(cst.PREFIX_MSG + 'Stopping PM2...');

  Satan.executeRemote('notifyKillPM2', {}, function() {});
  CLI.killAllModules(function() {
    CLI._operate('deleteProcessId', 'all', function(err, list) {
      Common.printOut(cst.PREFIX_MSG + 'All processes have been stopped and deleted');

      InteractorDaemonizer.killDaemon(function(err, data) {
        Satan.killDaemon(function(err, res) {
          if (err) Common.printError(err);
          Common.printOut(cst.PREFIX_MSG + 'PM2 stopped');
          return cb ? cb(err, res) : Common.exitCli(cst.SUCCESS_EXIT);
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
  var ret = [];

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

/**
 * Converts CamelCase Commander.js arguments
 * to Underscore
 * (nodeArgs -> node_args)
 */
CLI._handleAttributeUpdate = function(opts) {
  var conf = Config.transCMDToConf(opts);

  if (typeof(conf.name) != 'string')
    delete conf.name;

  var argsIndex = 0;
  if (opts.rawArgs && (argsIndex = opts.rawArgs.indexOf('--')) >= 0)
    conf.args = opts.rawArgs.slice(argsIndex + 1);

  var appConf = verifyConfs(conf)[0];

  if (argsIndex == -1)
    delete appConf.args;
  if (appConf.name == 'undefined')
    delete appConf.name;

  delete appConf.exec_mode;

  if(util.isArray(appConf.watch) && appConf.watch.length === 0) {
    if(!~opts.rawArgs.indexOf('--watch'))
      delete appConf.watch
  }

  return appConf;
};

CLI.set = function(key, value, cb) {

  /**
   * Specific when setting pm2 password
   * Used for restricted remote actions
   * Also alert Interactor that password has been set
   */
  if (key.indexOf('pm2:passwd') > -1) {
    value = Password.generate(value);
    Configuration.set(key, value, function(err) {
      if (err)
        return cb ? cb({success:false, err:err }) : Common.exitCli(cst.ERROR_EXIT);
      InteractorDaemonizer.launchRPC(function(err) {
        if (err) {
          displayConf('pm2', function() {
            return cb ? cb(null, {success:true}) : Common.exitCli(cst.SUCCESS_EXIT);
          });
          return false;
        }
        InteractorDaemonizer.rpc.passwordSet(function() {
          InteractorDaemonizer.disconnectRPC(function() {
            displayConf('pm2', function() {
              return cb ? cb(null, {success:true}) : Common.exitCli(cst.SUCCESS_EXIT);
            });
          });
        });
        return false;
      });
    });
    return false;
  }

  /**
   * Set value
   */
  Configuration.set(key, value, function(err) {
    if (err)
      return cb ? cb({success:false, err:err }) : Common.exitCli(cst.ERROR_EXIT);

    var values = [];

    if (key.indexOf('.') > -1)
      values = key.split('.');

    if (key.indexOf(':') > -1)
      values = key.split(':');

    if (values && values.length > 1) {
      // The first element is the app name (module_conf.json)
      var app_name = values[0];

      process.env.PM2_PROGRAMMATIC = 'true';
      CLI.restart(app_name, function(err, data) {
        process.env.PM2_PROGRAMMATIC = 'false';
        if (!err)
          Common.printOut(cst.PREFIX_MSG + 'Module %s restarted', app_name);
        displayConf(app_name, function() {
          return cb ? cb(null, {success:true}) : Common.exitCli(cst.SUCCESS_EXIT);
        });
      });
      return false;
    }
    displayConf(null, function() {
      return cb ? cb(null, {success:true}) : Common.exitCli(cst.SUCCESS_EXIT);
    });
  });
};

/**
 * Verify configurations.
 * @param {Array} appConfs
 * @returns {Array}
 */
function verifyConfs(appConfs){
  if (!appConfs || appConfs.length == 0){
    return [];
  }

  // Make sure it is an Array.
  appConfs = [].concat(appConfs);

  var verifiedConf = [];

  for (var i = 0; i < appConfs.length; i++) {
    var app = appConfs[i];

    // Warn deprecates.
    checkDeprecates(app);

    // Check Exec mode
    checkExecMode(app);

    // Render an app name if not existing.
    prepareAppName(app);

    debug('Before processing', app);
    // Verify JSON.
    var ret = Config.validateJSON(app);
    debug('After processing', ret);

    // Show errors if existing.
    if (ret.errors && ret.errors.length > 0){
      ret.errors.forEach(function(err){
        warn(err);
      });
      // Return null == error
      return null;
    }
    verifiedConf.push(ret.config);
  }

  return verifiedConf;
}

/**
 * Show warnings
 * @param {String} warning
 */
function warn(warning){
  Common.printOut(cst.PREFIX_MSG_WARNING + warning);
}
