def call() {
    def config = [
        // ✅ Current: Only enhancements (your resource constraint)
        activeBranches: ['enhancements'],

        // ✅ Production candidate branches (get notifications/deploy)
        productionCandidateBranches: ['main', 'enhancements'],

        // ✅ Development branches (get full pipeline)
        developmentBranches: ['enhancements'],

        // ✅ Experimental branches (future feature branches + improvements)
        experimentalBranches: ['feature/*', 'bugfix/*', 'improvements']
    ]

    // ✅ Compute branches that get full pipeline (active + experimental matches)
    def branchName = env.BRANCH_NAME ?: 'unknown'
    config.pipelineBranches = config.activeBranches + config.experimentalBranches.findAll { pattern ->
        branchName.matches(pattern.replace('*', '.*'))
    }.collect { branchName } // Add current branch if it matches experimental patterns

    echo "🔧 Branch configuration loaded: ${config.activeBranches.size()} active branches"
    echo "🔧 Current branch '${branchName}' gets full pipeline: ${config.pipelineBranches.contains(branchName)}"
    return config
}