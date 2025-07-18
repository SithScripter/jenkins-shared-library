def call(String suiteName, String buildNumber) {
    def summaryFile = "reports/${suiteName}-failure-summary.txt"
    def hasFailures = fileExists(summaryFile) && readFile(summaryFile).trim().toLowerCase().contains("failed")

    def failureSummary = hasFailures ? readFile(summaryFile).trim() : "✅ All tests passed."
    def failureHeader = hasFailures ? "❌ Failure Summary" : "✅ Test Result Summary"
    def failureBoxColor = hasFailures ? "#fff3f3" : "#f3fff3"
    def failureBorderColor = hasFailures ? "#f44336" : "#4CAF50"
    def failureTextColor = hasFailures ? "#c62828" : "#2e7d32"

    def html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>${suiteName.capitalize()} Test Dashboard</title>
            <style>
                body { font-family: Arial; padding: 20px; background-color: #f7f7f7; }
                h1 { color: #222; }
                ul { list-style-type: none; padding-left: 0; }
                li { margin: 10px 0; }
                a { color: #1976D2; font-size: 16px; text-decoration: none; }
                a:hover { text-decoration: underline; }
                .summary-box {
                    background-color: ${failureBoxColor};
                    border-left: 6px solid ${failureBorderColor};
                    padding: 10px;
                    margin-top: 20px;
                    white-space: pre-line;
                    font-family: monospace;
                    color: ${failureTextColor};
                }
            </style>
        </head>
        <body>
            <h1>📊 ${suiteName.capitalize()} Test Dashboard</h1>
            <ul>
                <li>🧪 <a href="chrome/index.html" target="_blank">Chrome Report</a></li>
                <li>🧪 <a href="firefox/index.html" target="_blank">Firefox Report</a></li>
            </ul>
            <p><strong>Build:</strong> #${buildNumber}</p>
            <h2>${failureHeader}</h2>
            <div class="summary-box">${failureSummary}</div>
        </body>
        </html>
    """

    writeFile file: 'reports/index.html', text: html
    echo "📝 HTML dashboard generated for suite: ${suiteName}"
}
