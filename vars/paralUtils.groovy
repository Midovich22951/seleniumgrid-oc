import org.apache.tools.ant.DirectoryScanner;


//
// Partition an array into X parts of ~even size 
//
Map partition(testList, nodeList) throws Exception {
    if ((nodeList == null) || (nodeList.size() == 0)) {
        throw new Exception("No nodes to execute tests");
    }
    int nodeCount = nodeList.size();
    if (testList.size() < nodeCount) {
        nodeCount = testList.size();
    }
    Map<String, List> testsByNodes = new HashMap<String, List>(nodeCount);
    for (int nodeIdx = 0; nodeIdx < nodeCount; nodeIdx++) {
        testsByNodes.put(nodeList[nodeIdx], new ArrayList());
    }

    int nodeIdx = 0;
    for (int testIdx = 0; testIdx < testList.size(); testIdx++) {
        if (nodeIdx == nodeCount) {
            nodeIdx = 0;
        }
        testsByNodes.get(nodeList[nodeIdx]).add(testList[testIdx]);
        nodeIdx++;
    }
    return testsByNodes;
}

//
// Get all Test cases given a Maven regexp, from a given path dir. 
//
def getTestsByRegexp(String path, String regexp) {
    DirectoryScanner scanner = new DirectoryScanner();
    if (regexp != null) {
        log("Input provided: " + regexp);
        String[] rules = regexp.split(",");
        String[] fullRules = new String[rules.size()];
        int i = 0

        rules.each {
            String prefix = "**/"
            String suffix = "*"
            String rule = ""
            it = it.trim(); // to remove any ' ' between rules if any
            if (it.startsWith(prefix)) {
                rule = it;
            } else {
                rule = prefix + it;
            }

            if (!it.endsWith(suffix)) {
                rule += suffix;
            }
            // else  NOthing  fullRules[i++] = it;
            // }

            fullRules[i++] = rule
        }
        log("Rules: $fullRules");
        scanner.setIncludes(fullRules);
    } else {
        println("NO input provided, defaulting to: Test*");
        scanner.setIncludes("Test*".split(","));
    }
    scanner.setBasedir(path);
    scanner.setCaseSensitive(false);
    scanner.scan();

    String[] files = scanner.getIncludedFiles();
    String[] testCases = new String[files.length];
    int i = 0;
    files.each {
        def file = it.replace("/", ".").replace("\\", ".");
        file = file.replace(".groovy", "");
        file = file.substring(file.lastIndexOf('.') + 1);
        log("FILE: $file");
        testCases[i++] = file;
    }
    return testCases;
}

//
// Search nodes where to build on, at least 1 idle exec...
// 
List getNodeList() {
    List<String> nodeList = new ArrayList<String>();
    def nodes = Jenkins.instance.getLabel("ANRJENKINS").getNodes();
    nodes.each {
        log("Idle Exec: for $it :  " + it.computer.countIdle());
        if (it.computer.countIdle() > 0){
            nodeList.add(it.displayName);
        }
    }
    return nodeList;
}

//
// Split the tests given regexp and the number of slaves 
// 
Map splitTests(String path, String regexp) throws Exception {
    String[] testList = getTestsByRegexp(path, regexp);
    if ((testList == null) || (testList.size() == 0)) {
        throw new Exception("No tests matching regexp: " + regexp);
    }
    log("--There are " + testList.size() + " tests matching regexp. List: \n " + testList);
    List<String> nodeList = getNodeList();
    log("---Found $nodeList slaves to run parallel tests");
    Map testsByNodes = partition(testList, nodeList);
    log("---- split tests: " + testsByNodes);
    return testsByNodes;
}

//
// Manage the parallelization of tests 
//
def parallelize_tests(Map testsByNodes, String type, String inclusionsFile, String results, Closure prepare, Closure run) {
    log("SPLITS: $splits");
    Map branches = [:];
    testsByNodes.each { nodeName, tests ->
        log("nodeName: $nodeName ==> tests: $tests");
        branches["${type}_${nodeName}"] = {
            stage("${type}_${nodeName}") {
                node(nodeName) {
                    // prepare tests for this node
                    prepare();
                    writeFile file: inclusionsFile, text: tests.join("\n");
                    // if (debugMode){
                    // sh "cat inclusions.txt";
                    // }
                    //run tests for this node
                    run();

                    //publish junits for this node
                    if (results != null) {
                        junit results;
                    }
                }
            }
        }
    }
    parallel branches
}
