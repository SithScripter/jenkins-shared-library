def call() {
    def causes = currentBuild.getBuildCauses()
    def descs = causes*.shortDescription.join(', ')
    echo "🔍 Build was triggered by: ${descs}"
  
    def isTimerTrigger  = descs.toLowerCase().contains('timer') || descs.toLowerCase().contains('cron')
    def isManualTrigger = descs.toLowerCase().contains('started by user')
  
    def suiteToRun = isTimerTrigger ? 'regression' : (params.SUITE_NAME ?: 'smoke')
    
    echo "✅ Pipeline will run the '${suiteToRun}' suite."
    echo "DEBUG: params.SUITE_NAME='${params.SUITE_NAME}', suiteToRun='${suiteToRun}'"
    
    return suiteToRun
}