var execSync = require('sync-exec');
var fs = require('fs-extra');
var gulp = require('gulp');
var prompt = require('gulp-prompt');
var runSequence = require('run-sequence');

var paths = {
    'webapp': 'ui',
    'webapp-build': 'ui/public',
    'webapp-build-javascripts': 'ui/assets/javascripts',
    'cordova': 'www',
    'cordova-js': 'platforms/android/assets/www',
    'cordova-plugin-build': 'plugins'
};

function updateContentSrc(source) {
    var file = 'config.xml';
    var fileData = fs.readFileSync(file, 'utf8');
    var replacedFileData = fileData.replace(new RegExp(/<content src.+>/), '<content src="'+ source +'"/>');
    fs.writeFileSync(file, replacedFileData);
}

gulp.task('clean-cordova-impl', function(){
    fs.removeSync(paths['cordova']);
    fs.emptyDir(paths['cordova']);
});

gulp.task('build-impl', function(){
    var cwd = process.cwd();
    process.chdir(paths.webapp);
    var mimosaBuildResults = execSync('mimosa build');
    console.log(mimosaBuildResults.stdout);
    console.error(mimosaBuildResults.stderr);
    process.chdir(cwd);

    fs.copySync(paths['webapp-build'], paths['cordova']);
});

gulp.task('run-live-reload-server-impl', function(){

    fs.copySync(paths['cordova-js'], paths['webapp-build-javascripts']);

    var cwd = process.cwd();
    process.chdir(paths.webapp);
    var mimosaBuildResults = execSync('mimosa watch --server');
    console.log(mimosaBuildResults);
});

function buildBackend() {
    var pluginPath = process.cwd()+'\\radio';
    var removeRadioResult = execSync('cordova plugin remove radio');
    console.log(removeRadioResult.stdout);
    console.error(removeRadioResult.stderr);
    fs.removeSync(paths['cordova-plugin-build']);
    fs.emptyDir(paths['cordova-plugin-build']);

    var addRadioResult = execSync('cordova plugin add ' + pluginPath);
    console.log(addRadioResult.stdout);
    console.error(addRadioResult.stderr);

    var buildResult = execSync('cordova build android');
    console.log(buildResult.stdout);
    console.error(buildResult.stderr);
}

gulp.task('build-backend-impl', function(){
    updateContentSrc('index.html');
    buildBackend();
});

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

gulp.task('build-backend-live-reload-impl', function(){
    return gulp.src('config.xml')
        .pipe(prompt.prompt({
            type: 'list',
            name: 'host',
            message: 'Select your host:',
            choices: getHosts()
        }, function(res){
            updateContentSrc('http://' + res.host);
            buildBackend();
        }));
});

gulp.task('deploy-impl', function(){
    var runResult = execSync('cordova run android');
    console.log(runResult.stdout);
    console.error(runResult.stderr);
});

gulp.task('kill', function(){
    process.exit(0);
});

gulp.task('clean', function(){
    runSequence('clean-cordova-impl', 'kill');
});

gulp.task('build', function(){
    runSequence('clean-cordova-impl', 'build-impl', 'kill');
});

gulp.task('build-backend', function(){
    runSequence('build-backend-impl', 'kill');
});

gulp.task('build-and-deploy-live-reload', function(){
    runSequence('clean-cordova-impl', 'build-backend-live-reload-impl', 'deploy-impl', 'run-live-reload-server-impl');
});

gulp.task('build-and-deploy', function(){
    runSequence('clean-cordova-impl','build-impl', 'build-backend-impl', 'deploy-impl', 'kill');
});