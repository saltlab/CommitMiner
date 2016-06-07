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
