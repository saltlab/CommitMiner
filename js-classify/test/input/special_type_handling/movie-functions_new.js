/*
	MediaCenterJS - A NodeJS based mediacenter solution
	
    Copyright (C) 2014 - Jan Smolders

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* Global Imports */
var fs = require('fs.extra')
	, file_utils = require('../../lib/utils/file-utils')
	, app_cache_handler = require('../../lib/handlers/app-cache-handler')
	, colors = require('colors')
	, os = require('os')
    , metafetcher = require('../../lib/utils/metadata-fetcher')
	, config = require('../../lib/handlers/configuration-handler').getConfiguration();

var dblite = require('dblite')
if(os.platform() === 'win32'){
    dblite.bin = "./bin/sqlite3/sqlite3";
}
var db = dblite('./lib/database/mcjs.sqlite');
db.on('info', function (text) { console.log(text) });
db.on('error', function (err) { console.error('Database error: ' + err) });


exports.loadItems = function (req, res, serveToFrontEnd){
    var metaType = "movie";
    if(serveToFrontEnd === false){
        fetchMovieData(req, res, metaType, serveToFrontEnd);
    } else if(serveToFrontEnd === undefined || serveToFrontEnd === null){
        var serveToFrontEnd = true; 
        getMovies(req, res, metaType, serveToFrontEnd);
    } else{
        serveToFrontEnd = true; 
        getMovies(req, res, metaType, serveToFrontEnd);
    }
};


exports.backdrops = function (req, res){
    db.query('SELECT * FROM movies',{
        original_name  	: String,
        title 		    : String,
        poster_path  	: String,
        backdrop_path  	: String,
        imdb_id  		: String,
        rating  		: String,
        certification  	: String,
        genre  			: String,
        runtime  		: String,
        overview  		: String,
        cd_number  		: String,
        adult           : String
    }, function(rows) {
        if (rows !== null && rows !== undefined){
            var backdropArray = [];
            rows.forEach(function(item){
               var backdrop = item.backdrop_path;
               backdropArray.push(backdrop)
            });
            res.json(backdropArray);
        } else {
            console.log('Could not index any movies, please check given movie collection path');
        }
    });
};



exports.playMovie = function (req, res, movieTitle){
	file_utils.getLocalFile(config.moviepath, movieTitle, function(err, file) {
		if (err) console.log(err .red);
		if (file) {
			var movieUrl = file.href
			, movie_playback_handler = require('./movie-playback-handler');

            var subtitleUrl = movieUrl;
            subtitleUrl = subtitleUrl.split(".");
            subtitleUrl = subtitleUrl[0]+".srt";

            var subtitleTitle = movieTitle;
            subtitleTitle = subtitleTitle.split(".");
            subtitleTitle = subtitleTitle[0]+".srt";

            movie_playback_handler.startPlayback(res, movieUrl, movieTitle, subtitleUrl, subtitleTitle);
    
		} else {
			console.log("File " + movieTitle + " could not be found!" .red);
		}
	});
};

exports.getGenres = function (req, res){
	db.query('SELECT genre FROM movies', function(rows) {
		if (typeof rows !== 'undefined' && rows.length > 0){
			var allGenres = rows[0][0].replace(/\r\n|\r|\n| /g,","),
				genreArray = allGenres.split(',');
			res.json(genreArray);
		}
	});
};

exports.filter = function (req, res, movieRequest){
	db.query('SELECT * FROM movies WHERE genre =?', [movieRequest], { local_name: String }, function(rows) {
		if (typeof rows !== 'undefined' && rows.length > 0) {
			res.json(rows);
		}
	});
};

exports.sendState = function (req, res){ 
    db.query("CREATE TABLE IF NOT EXISTS progressionmarker (movietitle TEXT PRIMARY KEY, progression TEXT, transcodingstatus TEXT)");

    var incommingData   = req.body
    , movieTitle        = incommingData.movieTitle
    , progression       = incommingData.currentTime
    , transcodingstatus = 'pending';
    
    if(movieTitle !== undefined && progression !== undefined){
        var progressionData = [movieTitle, progression, transcodingstatus];
        db.query('INSERT OR REPLACE INTO progressionmarker VALUES(?,?,?)', progressionData);
    }
   
}


/** Private functions **/

fetchMovieData = function(req, res, metaType, serveToFrontEnd) {
    metafetcher.fetch(req, res, metaType, function(type){
        if(type === metaType){
            getMovies(req, res, metaType, serveToFrontEnd);
        }
    });             
}

getMovies = function(req, res, metaType, serveToFrontEnd){
    db.query('SELECT * FROM movies',{
        original_name  	: String,
        title 		    : String,
        poster_path  	: String,
        backdrop_path  	: String,
        imdb_id  		: String,
        rating  		: String,
        certification  	: String,
        genre  			: String,
        runtime  		: String,
        overview  		: String,
        cd_number  		: String,
        adult           : String
    }, 
    function(err, rows) {
        if(err){
            console.log("DB error",err);
            serveToFrontEnd = true;
            fetchMovieData(req, res, metaType, serveToFrontEnd);
        }
        if (rows !== null && rows !== undefined){
            if(serveToFrontEnd !== false){
                res.json(rows);
            }
        } else {
            serveToFrontEnd = true;
            fetchMovieData(req, res, metaType, serveToFrontEnd);
        }
    });
}