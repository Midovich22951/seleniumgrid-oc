def call(String tenant, String password, String acs_instance, String project, String template_file) {
  echo "Executing template '${template_file}' in project '${project}' on ACS instance '${acs_instance}' as user '${tenant}'"

  sh "oc login --username='${tenant}' --password='${password}' ${acs_instance} --insecure-skip-tls-verify"
  sh "oc project ${project}"
  def return_code = sh (returnStatus: true, script: "oc process --filename ${template_file} | oc create -f -")
  if (return_code != 0) {
    sh "oc process --filename ${template_file} | oc apply -f -"
  }
  sh "oc logout"
}