
module.exports = function(grunt) {

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        build: {
            path: 'build/<%= pkg.version %>',
            vendor: '<%= build.path %>/vendor'
        },
        clean: {
            dist: ['<%= build.path %>/*']
        },
        jshint: {
            files: ['Gruntfile.js', 'src/**/*.js'],
            options: {
                globals: {
                    jQuery: true
                }
            }
        },
        watch: {
            files: ['<%= jshint.files %>'],
            tasks: ['jshint']
        },
        copy: {
            dist: {
                files: [
                    {   expand: true, cwd: './bower_components', src: ['backbone/backbone.js'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['underscore/underscore-min.js'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['fontawesome/**'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['jquery/dist/jquery.min.js'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['qtip2/jquery.qtip.min.css'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['qtip2/jquery.qtip.min.js'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['uri.js/src/URI.min.js'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['polymer/polymer.html'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['iron-*/**'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['paper-*/**'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['webcomponentsjs/webcomponents-lite.min.js'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './src', src: ['**'], dest: '<%= build.path %>/' },
                    {   expand: true, cwd: './', src: ['LICENSE'], dest: '<%= build.path %>/' },
                ]
            }
        },
        processhtml: {
            options: {
                strip: true
            },
            dist: {
                files: {
                    '<%= build.path %>/index.html': ['src/index.html']
                }
            }
        },
        replace: {
            dist: {
                options: {
                    patterns: [
                        {
                            match: /..\/bower_components/g,
                            replacement: 'vendor'
                        }
                    ]
                },
                files: [
                    {expand: true, flatten: true, src: ['<%= build.path %>/*.html'], dest: '<%= build.path %>'}
                ]
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-processhtml');
    grunt.loadNpmTasks('grunt-replace');

    grunt.registerTask('default', ['clean', 'jshint', 'copy', 'replace']);
    grunt.registerTask('cl', ['clean']);
    // grunt.registerTask('test', ['clean']);
};