/**
 * Module dependencies
 */

var fs        = require('fs');
var path      = p = require('path');
var util      = require('util');
var cronJob   = require('cron').CronJob;

var cst       = require('../constants.js');

/**
 * Common methods (used by CLI and God)
 */
  
var Common = module.exports;

/**
 * Resolve app paths and replace missing values with defaults.
 * @param app {Object}
 * @param [cwd] {string} Optional cwd for app. Defaults to `process.cwd()`.
 * @returns {Object}
 */
Common.resolveAppPaths = function(app, cwd) {

  cwd = cwd || process.cwd();
  
  app.env = {
    pm_cwd: cwd
  };

  if (!('exec_mode' in app)) app['exec_mode'] = 'cluster_mode';

  app["pm_exec_path"] = path.resolve(cwd, app.script);
  delete app.script;

  if (!app["name"]) {
    app["name"] = p.basename(app["pm_exec_path"]);
  }


  if (fs.existsSync(app.pm_exec_path) == false) {
    console.error(cst.PREFIX_MSG_ERR + 'script not found : ' + app.pm_exec_path);
    process.exit(cst.ERROR_EXIT);
  }

  if (app.out_file)
    app["pm_out_log_path"] = path.resolve(cwd, app.out_file);
  else {
    if (!app.name) {
      console.error(cst.PREFIX_MSG_ERR + 'You havent specified log path, please specify at least a "name" field in the JSON');
      process.exit(cst.ERROR_EXIT);
    }
    app["pm_out_log_path"] = path.resolve(cst.DEFAULT_LOG_PATH, [app.name, '-out.log'].join(''));
    app.out_file = app["pm_out_log_path"];
  }
  delete app.out_file;

  if (app.error_file)
    app["pm_err_log_path"] = path.resolve(cwd, app.error_file);
  else {
    app["pm_err_log_path"] = path.resolve(cst.DEFAULT_LOG_PATH, [app.name, '-err.log'].join(''));
    app.error_file = app["pm_err_log_path"];
  }
  delete app.error_file;

  if (app.pid_file)
    app["pm_pid_path"] = path.resolve(cwd, app.pid_file);
  else {
    app["pm_pid_path"] = path.resolve(cst.DEFAULT_PID_PATH, [app.name, '.pid'].join(''));
    app.pid_file = app["pm_pid_path"];
  }
  delete app.pid_file;

  // Set current env
  util._extend(app.env, process.env);

  //set port env variable
  if (app.port) {
    app.env.PORT = app.port;
  }

  return app;
};

Common.validateApp = function(appConf, outputter) {
  var instances = appConf['instances'];
  var script    = appConf['script'];
  var cron_pattern = appConf['cron_restart'];

  if (instances && isNaN(parseInt(instances)) && instances != 'max') {
    return new Error(cst.PREFIX_MSG_ERR + 'Instance option must be an integer or the "max" string');
  }

  if (cron_pattern) {
    try {
      console.log(cron_pattern);
      var cron_test = new cronJob(cron_pattern, function() {
        if (outputter)
          outputter(cst.PREFIX_MSG + 'cron pattern for auto restart detected and valid');
        delete cron_test;
      });
    } catch(ex) {
      return new Error(cst.PREFIX_MSG_ERR + 'Cron pattern is not valid !');
    }
  }
  
  return null;
};