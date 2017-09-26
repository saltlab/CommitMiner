/**
 * Copyright 2013 the PM2 project authors. All rights reserved.
 * Use of this source code is governed by a license that
 * can be found in the LICENSE file.
 */

var CLI                  = module.exports = {};

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
