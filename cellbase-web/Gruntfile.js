
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
        concat: {
            options: {
                sourceMap: true
            },
            vendors: {
                src: [
                    './bower_components/webcomponentsjs/webcomponents-lite.js',
                    './bower_components/jquery/dist/jquery.js',
                    './bower_components/underscore/underscore.js',
                    './bower_components/backbone/backbone.js'
                ],
                dest: '<%= build.path %>/vendors.js'
            }
            // jsorolla: {
            //     src: [
            //         './lib//jsorolla/src/lib/clients/cellbase-client.js',
            //         './lib/jsorolla/src/lib/cache/indexeddb-cache.js',
            //         '/lib/jsorolla/src/lib/clients/cellbase-client-config.js'
            //     ],
            //     dest: '<%= build.path %>/jsorolla.js'
            // }
        },
        uglify: {
            options: {
                banner: '/*! CellBase <%= grunt.template.today("dd-mm-yyyy") %> */\n'
            },
            dist: {
                files: {
                    '<%= build.path %>/vendors.min.js': ['<%= build.path %>/vendors.js']
                    // '<%= build.path %>/jsorolla.min.js': ['<%= build.path %>/jsorolla.js']
                }
            }
        },
        copy: {
            dist: {
                files: [
                    // {   expand: true, cwd: './bower_components', src: ['backbone/backbone-min.js'], dest: '<%= build.vendor %>' },
                    // {   expand: true, cwd: './bower_components', src: ['underscore/underscore-min.js'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['fontawesome/**'], dest: '<%= build.vendor %>' },
                    // {   expand: true, cwd: './bower_components', src: ['jquery/dist/jquery.min.js'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['qtip2/jquery.qtip.min.css'], dest: '<%= build.vendor %>' },
                    // {   expand: true, cwd: './bower_components', src: ['qtip2/jquery.qtip.min.js'], dest: '<%= build.vendor %>' },
                    // {   expand: true, cwd: './bower_components', src: ['uri.js/src/URI.min.js'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: './bower_components', src: ['polymer/polymer.html'], dest: '<%= build.vendor %>' },
                    // {   expand: true, cwd: './bower_components', src: ['iron-*/**'], dest: '<%= build.vendor %>' },
                    // {   expand: true, cwd: './bower_components', src: ['paper-*/**'], dest: '<%= build.vendor %>' },
                    // {   expand: true, cwd: './bower_components', src: ['webcomponentsjs/webcomponents-lite.min.js'], dest: '<%= build.vendor %>' },
                    {   expand: true, cwd: 'src', src: ['index.html'], dest: '<%= build.path %>/' },
                    {   expand: true, cwd: 'src', src: ['config.js'], dest: '<%= build.path %>/' },
                    {   expand: true, cwd: './', src: ['LICENSE'], dest: '<%= build.path %>/' },
                    {   expand: true, cwd: 'src', src: ['components/**'], dest: '<%= build.path %>/' },
                    {   expand: true, cwd: './lib', src: ['ChemDoodle/**'], dest: '<%= build.path %>/' },
                    {   expand: true, cwd: './lib', src: ['jsorolla/**'], dest: '<%= build.path %>/' }
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
        vulcanize: {
            default: {
                options: {
                    // Task-specific options go here.
                    stripComments: true
                },
                files: {
                    // Target-specific file lists and/or options go here.
                    '<%= build.path %>/build.html': 'src/index.html'
                }
            }
        },
        watch: {
            files: ['<%= jshint.files %>'],
            tasks: ['jshint']
        },
        replace: {
            dist: {
                options: {
                    patterns: [
                        {
                            match: /\.\.\/bower_components/g,
                            replacement: 'vendor'
                        },
                        {
                            match: /\.\.\/lib\//g,
                            replacement: ''
                        },
                    ]
                },
                files: [
                    {expand: true, flatten: true, src: ['<%= build.path %>/index.html'], dest: '<%= build.path %>'}
                ]
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-processhtml');
    grunt.loadNpmTasks('grunt-vulcanize');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-replace');

    grunt.registerTask('default', ['clean', 'jshint', 'copy', 'concat'  , 'processhtml', 'replace', 'vulcanize']);
    grunt.registerTask('cl', ['clean']);
    // grunt.registerTask('test', ['clean']);
};