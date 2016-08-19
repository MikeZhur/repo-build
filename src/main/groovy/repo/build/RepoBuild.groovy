package repo.build

import groovy.util.CliBuilder;

class RepoBuild {
    final CliBuilder cli
    final String[] args
    OptionAccessor options
    RepoEnv env

    RepoBuild(args) {
        this.cli = CliBuilderFactory.build()
        this.args = args
    }

    static main(args) {
        def repoBuild = new RepoBuild(args)
        try {
            repoBuild.execute()
        }
        catch(Exception e) {
            e.printStackTrace()
            System.out.println(e.message)
        }
    }



    void execute() {
        options = cli.parse(args)
        env = new RepoEnv(getRepoBasedir())
        println getRepoBasedir()

        def commands = options.arguments();
        commands.each {
            switch(it) {
                case "build-pom":
                    doBuildPom()
                    break;
                case "switch":
                    doSwitch()
                    break;
                case "prepare-merge":
                    doPrepareMerge()
                    break;
                case "import-bundles":
                    doImportBundles()
                    break;
                case "export-bundles":
                    doExportBundles()
                    break;
                case "init":
                    doInit()
                    break;
                case "sync":
                    doSync()
                    break;
                default:
                    println cli.usage()
            }
        }
    }

    File getRepoBasedir() {
        return options.r ? new File(options.r) : new File(".").getAbsoluteFile()
    }

    void doBuildPom() {
        def buildPomFile = options.p ?
                new File(options.p) :
                new File(getRepoBasedir(),"pom.xml")
        def featureBranch = options.f ?
                options.f :
                RepoManifest.getBranch(env, "manifest")
        Pom.generateXml(env, featureBranch, buildPomFile)
    }

    void doSwitch() {
        def featureBranch = options.f
        if( featureBranch ) {
            RepoManifest.switchToBranch(env, featureBranch)
        } else {
            throw new RepoBuildException("featureBranch required")
        }
    }

    void doPrepareMerge() {
        def featureBranch = getRequired(options.f,"featureBranch")
        if( featureBranch) {
            RepoManifest.mergeFeatureBranch(env, featureBranch )
        } else {
            throw new RepoBuildException("featureBranch required")
        }
    }

    void doImportBundles() {
    }

    void doExportBundles() {
        def featureBranch = getRequired(options.f,"featureBranch")
        def targetExportDir = options.t ?
                new File(options.t)
                : new File(getRepoBasedir(), featureBranch)

        targetExportDir.mkdirs()
        RepoManifest.createFeatureBundles(env, featureBranch, targetExportDir )
    }

    void doInit() {
        def manifestDir = new File(getRepoBasedir(), "manifest")
        def manifestUrl = options.M
        if(!env.manifest) {
            if(manifestUrl) {
                Git.clone(env, manifestUrl, "origin", manifestDir )
                env.openManifest()
            } else {
                throw new RepoBuildException("manifestUrl required")
            }
        }
        def manifestBranch = getRequired(options.M, "manifestBranch")
        Git.checkoutUpdate(env, manifestBranch, "origin/$manifestBranch", manifestDir)
    }

    static void doSync(options) {
    }

    String getRequired(value, name) {
        if(value) {
            return value;
        } else {
            throw new RepoBuildException("$name required")
        }
    }
}
