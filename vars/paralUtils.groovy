def checkoutRepo(git_url, branch, ref_specs) {// pull_request to be reviewed...?????
    def credentialsId =  "$ANR_USER_CRED"; // Global Env. Var.
    def default_branch_name = branch; 
    if ("$ref_specs"){
	default_branch_name = "origin/mainline";
    }   
    echo "Working on gitURL is : $git_url";
    echo "Checking out $default_branch_name";

    checkout ([  $class: 'GitSCM', 
                branches: [[
                        name: "$default_branch_name"
                        ]], 
                doGenerateSubmoduleConfigurations: false, 
                extensions: [[ $class: 'WipeWorkspace']], 
                submoduleCfg: [], 
                userRemoteConfigs: [[
                        credentialsId: "$credentialsId", 
                        url: "$git_url"
                        ]]
                ])
	
	if ("$ref_specs") { //2nd try necessary when pull-request... not clear why...
	    checkout ([  $class: 'GitSCM', 
			 branches: [[
				     name: "$branch"
				     ]], 
			 doGenerateSubmoduleConfigurations: false, 
			 submoduleCfg: [], 
			 userRemoteConfigs: [[
					      credentialsId: "$credentialsId", 
					      refspec: "$ref_specs", 
					      url: "$git_url"
					      ]]
			 ])
	 }   
}