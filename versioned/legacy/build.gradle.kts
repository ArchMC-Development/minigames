repositories {
    maven("https://repo.glaremasters.me/repository/concuncan/")
}

dependencies {
    compileOnly(project(":versioned:generics"))
    compileOnly(fileTree("spigot"))
    compileOnly("com.grinderwolf:slimeworldmanager-plugin:2.2.1")
    compileOnly("mc.arch.worldedit:WorldEdit:1.0.0")
}
