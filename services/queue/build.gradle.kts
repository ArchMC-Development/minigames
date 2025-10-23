dependencies {
    compileOnly(project(":shared"))
    compileOnly(project(":services:application:api"))
    compileOnly(project(":services:replications"))
    compileOnly(project(":services:games:game-manager"))

    api("lol.arch.symphony:api:1.1.1")

    compileOnly(project(":minigames:skywars-shared"))
    compileOnly(project(":minigames:bedwars-shared"))
    compileOnly(project(":microgames:events-api"))

    implementation("net.md-5:bungeecord-chat:1.20-R0.1")
}
