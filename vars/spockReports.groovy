import groovy.json.JsonSlurper;


@NonCPS
def generateSpockReport(){
    def binding =  [:];
    def jsonSlurper = new JsonSlurper();
    
    def aggregate_json = new File("${WORKSPACE}/spock-reports/aggregated.json").text;
    def object = jsonSlurper.parseText(aggregate_json);
    binding.data = object;
          
    def eng = new groovy.text.GStringTemplateEngine();
    File templateFile = new File("${WORKSPACE}/spock-reports/summary-template.html");
    def tpl = eng.createTemplate(templateFile).make(binding);    
    writeFile file: 'spock-reports/full_summary.html', text:  tpl.toString()
}

//
/// Build a new json file by concatenation of individual jsons
//
def concatAggregate(){
    File spockDir = new File("${WORKSPACE}/spock-reports");
    if (! spockDir.isDirectory()) throw new Exception("No spock report directory found!");  

    def data = "{";     
    def first = true; //first loop (to manage ',' separator)

    File[] aggregs = spockDir.listFiles();
    for(File aggreg: aggregs) {
        String fullName = aggreg.getAbsolutePath();
        if (fullName.contains("aggregated_report")){
             String aggregate_json = new File(aggreg.getAbsolutePath()).text;
             aggregate_json = aggregate_json.substring(1, aggregate_json.length()-1);//remove first { and last } chars
             if (! first) data += ",";
             first=false;
             data += aggregate_json;
	}
    }
    data += "}"; //finalize array
    if (data.length() == 2) throw new Exception("No aggregate data found")  ;
    writeFile file: 'spock-reports/aggregated.json', text:  data ;
}

def patchSummaryTemplate(){
    // Patch the summary template file
    writeFile file: 'temp.txt', text: "def stats = [ total: 0, passed: 0, failed: 0, fFails: 0, fErrors: 0, time: 0.0, successRate: 0.0 ] \n\
  data.values().each { Map json -> \n					\
            def sta = json.stats \n					\
            def isFailure = sta.failures + sta.errors > 0 \n\
            stats.total += 1 \n\
            stats.passed += ( isFailure ? 0 : 1 ) \n\
            stats.failed += ( isFailure ? 1 : 0 ) \n\
            stats.fFails += sta.failures \n\
            stats.fErrors += sta.errors \n\
            stats.time += sta.time \n	  \
} \n					  \
double dTotal = stats.total \n		  \
double dReproved = stats.failed \n					\
stats.successRate = Math.min( 1.0D, Math.max( 0.0D, ( dTotal > 0D ? ( dTotal - dReproved ) / stats.total : 1.0D ) ) ) \n \
    " ;
    sh 'sed -e "/def stats.*/r temp.txt" -e "s///"  -i spock-reports/summary-template.html'
}
