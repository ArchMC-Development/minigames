dependencies {
    compileOnly(project(":shared"))
    compileOnly(project(":services:games:game-manager"))
    compileOnly(project(":services:application:api"))
    compileOnly(project(":services:games:game-manager"))
    
    // Sentry for distributed tracing
    compileOnly("io.sentry:sentry:7.22.0")
}
