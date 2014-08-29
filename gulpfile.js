var gulp = require('gulp');
var execSync = require('execSync').run;
var fs = require('fs');

var paths = {
    'webapp': 'webapp',
    'webapp-build': 'webapp/public',
    'cordova': 'www'
};

gulp.task('build', function(){
    process.chdir(paths.webapp);
    var mimosaBuildResults = execSync('mimosa build -o -P phone-build');
    console.log(mimosaBuildResults);
});

function replaceAllInFile(file, target, source) {
    fs.readFile(file, 'utf8', function (err, data) {
        if (err) {
            return console.log(err);
        }
        var result = data.replace(new RegExp(target, 'g'), source);

        fs.writeFile(file, result, 'utf8', function (err) {
            if (err) return console.log(err);
        });
    });
}

var readline = require('readline');

var rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

function getHost() {
    rl.question('Enter your host (i.e. 192.168.0.101:3000):', function(host){
        console.log(amswer);
    });
}

function getHosts() {
    var ifaces = require('os').networkInterfaces();
    var hosts = [];
    for(var dev in ifaces) {
        ifaces[dev].forEach(function(details){
            if(details.family == 'IPv4')
                hosts.push(details.address+':3000');
        });
    }

    return hosts;
}

var prompt = require('gulp-prompt');

gulp.task('build-live-reload', function(){
    //Build
    var cwd = process.cwd();
    process.chdir(paths.webapp);
    var mimosaBuildResults = execSync('mimosa build -P phone-live-reload-build');
    console.log(mimosaBuildResults);
    process.chdir(cwd);

    //Replace host placeholder text
    

    return gulp.src(paths.cordova + '/index.html')
   .pipe(prompt.prompt({
        type: 'list',
        name: 'host',
        message: 'Select your host:',
        choices: getHosts()
    }, function(res){
        replaceAllInFile(paths.cordova + '/index.html', '{placeholderhost}', res.host);
    }));
});