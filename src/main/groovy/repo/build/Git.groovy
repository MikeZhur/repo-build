package repo.build
import java.io.File;

class Git {

    static final String PREPARE_BUILD = "prepareBuild"

    static boolean branchPresent( File dir, String branch ) {
        return ! ExecuteProcess.executeCmd0(dir, "git ls-remote . $branch").empty;
    }


    static void deleteBranch(File dir, String branch) {
        ExecuteProcess.executeCmd0(dir,"git branch -d $branch")
    }

    static void mergeFeatureBranch( RepoEnv env, String branch, String remoteBranch, String startCommit, File dir ) {
        ExecuteProcess.executeCmd0(dir,"git checkout -B $PREPARE_BUILD $startCommit")
        ExecuteProcess.executeCmd0(dir,"git merge $remoteBranch")
    }

    static void createFeatureBundle( RepoEnv env, String branch, File dir, File bundleFile ) {
        ExecuteProcess.executeCmd0(dir, "git bundle create $bundleFile $branch")
    }


    static void checkoutUpdate(RepoEnv env, String branch, String remoteBranch, File dir) {
        if(Git.branchPresent(dir, branch)) {
            ExecuteProcess.executeCmd0(dir, "git checkout $branch")
            if(branchPresent(dir, remoteBranch)) {
                ExecuteProcess.executeCmd0(dir, "git merge $remoteBranch")
            }
        } else if(branchPresent(dir, remoteBranch)) {
            ExecuteProcess.executeCmd0(dir, "git checkout $branch")
        }
    }

    static void clone( RepoEnv env, String url, String remoteName, File dir) {
        dir.mkdirs()
        ExecuteProcess.executeCmd0(dir, "git clone -o $remoteName $url .")
    }
}
