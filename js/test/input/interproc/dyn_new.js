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
