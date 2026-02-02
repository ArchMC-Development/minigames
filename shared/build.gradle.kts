repositories {
    maven("https://repo.glaremasters.me/repository/concuncan/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    api(project(":services:metadata"))
    compileOnly(project(":parties"))
    compileOnly(fileTree("spigot"))

    compileOnly(project(":versioned:generics"))
    compileOnly(project(":versioned:legacy"))
    compileOnly(project(":versioned:modern"))

    compileOnly("mc.arch.worldedit:WorldEdit:1.0.0")
    compileOnly("com.grinderwolf:slimeworldmanager-plugin:2.2.1")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")

    api("joda-time:joda-time:2.12.5")
    api("xyz.xenondevs:particle:1.7.1")
    api("lol.arch.symphony:api:1.1.0")
    
    // Sentry for distributed tracing
    compileOnly("io.sentry:sentry:7.22.0")
}
