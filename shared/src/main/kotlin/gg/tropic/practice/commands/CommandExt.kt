package gg.tropic.practice.commands

import gg.scala.basics.plugin.profile.BasicsProfile
import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import gg.tropic.practice.profile.PracticeProfile
import gg.tropic.practice.profile.PracticeProfileService
import net.evilblock.cubed.util.CC
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 10/20/2023
 */
val UUID.offlineProfile: PracticeProfile
    get() = PracticeProfileService.find(this)
        ?: DataStoreObjectControllerCache
            .findNotNull<PracticeProfile>()
            .load(this, DataStoreStorageType.MONGO)
            .join()
        ?: throw ConditionFailedException(
            "${CC.YELLOW}${username()}${CC.RED} has not logged onto our duels server."
        )

fun UUID.networkTransaction(block: PracticeProfile.() -> Unit): CompletableFuture<Void>
{
    val local = PracticeProfileService.find(this)
    if (local != null)
    {
        block(local)
        return local.save()
    }

    return PracticeProfileService.useAsyncThenSaveGlobally(this) {
        block(it)
        return@useAsyncThenSaveGlobally null
    }
}

val UUID.basicsProfile: BasicsProfile
    get() = BasicsProfileService.find(this)
        ?: DataStoreObjectControllerCache
            .findNotNull<BasicsProfile>()
            .load(this, DataStoreStorageType.MONGO)
            .join()
        ?: throw ConditionFailedException(
            "${CC.YELLOW}${username()}${CC.RED} has not logged onto our network."
        )

val unsignedNumberExtractor = "[0-9]\\d*".toRegex()
fun extractNumbers(string: String) = unsignedNumberExtractor
    .findAll(string)
    .sumOf { it.value.toInt() }
