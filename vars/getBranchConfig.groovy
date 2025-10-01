def call() {
    def config = [
        // ✅ Current: Only enhancements (your resource constraint)
        activeBranches: ['enhancements', 'improvements'],
        
        // ✅ Production candidate branches (get notifications/deploy)
        productionCandidateBranches: ['main', 'enhancements'],
        
        // ✅ Development branches (get full pipeline)
        developmentBranches: ['enhancements', 'improvements'],
        
        // ✅ Experimental branches (future feature branches)
        experimentalBranches: ['feature/*', 'bugfix/*', 'improvements']
    ]
    
    echo "🔧 Branch configuration loaded: ${config.activeBranches.size()} active branches"
    return config
}