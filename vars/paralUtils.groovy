//
// Partition an array into X parts of ~even size 
//
def partition(arrayList, nodes){
    def maxNodes = nodes.size();
    if (arrayList.size() < maxNodes){
	maxNodes = arrayList.size();
    }
    Map<String, List> resList = new HashMap<String, List>(maxNodes);
    def n = 0;
    for (n=0; n < maxNodes; n++){
	List nodeList = new  ArrayList();
	resList.put(nodes[n], nodeList);
    }
    
    n = 0;//reinit node count
    for (int i = 0; i < arrayList.size(); i++) {
	if (n == maxNodes){
	    n = 0;
	}
	resList.get(nodes[n]).add(arrayList[i]);
	n++;
    }
    return resList;
}

//
// Get all Test cases given a Maven regexp, from a given path dir. 
//
@NonCPS
def getTestsByRegexp(String path, String regexp){
   DirectoryScanner scanner = new DirectoryScanner();
    if (regexp != null) {
        log("Input provided: "+regexp);
        def rules = regexp.split(",");
        def fullRules = new String[rules.size()];
        def i = 0
       
        rules.each{
            def prefix = "**/"
            def suffix = "*"
            def rule = ""
            it = it.trim(); //to remove any ' ' between rules if any
            if (it.startsWith(prefix)){
                rule = it;
            }
            else {
                rule = prefix + it;
            }
            
            if (! it.endsWith(suffix)) {
                rule += suffix;
            }
            
            //else  NOthing  fullRules[i++] = it; 
	    //  }	    
	  
	    fullRules[i++] = rule 
        }
        log ("Rules: $fullRules");
        scanner.setIncludes(fullRules);
    }
    else {
        println("NO input provided, defaulting to: Test*");
        scanner.setIncludes("Test*".split(","));
    }
    scanner.setBasedir(path);
    scanner.setCaseSensitive(false);
    scanner.scan();
    
    String[] files = scanner.getIncludedFiles();
    String[] testCases = new String[files.length];
    def i =0;
    files.each {
	def file = it.replace("/", ".").replace("\\", ".");
	file = file.replace(".groovy", "");
	file = file.substring(file.lastIndexOf('.')+1);
    	log( "FILE: $file");
	testCases[i++] = file;
    }
    return testCases;
}

//
// Search nodes where to build on, at least 1 idle exec...
// 
def getNodeList(){
    List<String> nodeList = new ArrayList<String>(3);
    def nodes = Jenkins.instance.getLabel("ANRJENKINS").getNodes();
    nodes.each{
	log("Idle Exec: for $it :  " + it.computer.countIdle());
	if (it.computer.countIdle() > 0) nodeList.add(it.displayName);
    }    
    return nodeList;
}

//
// Split the tests given regexp and the number of slaves 
// 
def splitTests(String path, String regexp) throws Exception{
    String[] files =  getTestsByRegexp(path, regexp);
    if ((files == null) || (files.size() == 0)){
	throw new Exception("No tests matching regexp: " + regexp);
    }
    log("--There are " + files.size() + " tests matching regexp. List: \n " + files);    
    def nodeList = getNodeList();
    log("---Found $nodeList slaves to run parallel tests");
    def resList = partition(files, nodeList);
    log("---- split tests: " + resList);
    return resList;
}
