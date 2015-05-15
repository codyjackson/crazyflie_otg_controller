var gulp = require('gulp');
var execSync = require('execSync').run;
var fs = require('fs');

var paths = {
    'webapp': 'ui',
    'webapp-build': 'ui/public',
    'cordova': 'www'
};

gulp.task('build-impl', function(){
    var cwd = process.cwd();
    process.chdir(paths.webapp);
    var mimosaBuildResults = execSync('mimosa build -o -P phone-build');
    console.log(mimosaBuildResults);
    process.chdir(cwd);
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

gulp.task('build-live-reload-impl', function(){
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
        process.exit(0);
    }));
});

gulp.task('build-backend-impl', function(){
    var pluginPath = process.cwd()+'\\radio';
    console.log(execSync('cordova plugin remove radio'));
    console.log(execSync('cordova plugin add ' + pluginPath));
    console.log(execSync('cordova build android'));
});

gulp.task('deploy-impl', function(){
    console.log(execSync('cordova run android'));
});

gulp.task('kill', function(){
    process.exit(0);
});

gulp.task('build', ['build-impl', 'kill']);

gulp.task('build-backend', ['build-backend-impl', 'kill']);

gulp.task('build-and-deploy', ['build-impl', 'build-backend-impl', 'deploy-impl', 'kill']);