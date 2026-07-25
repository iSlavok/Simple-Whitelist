package online.slavok.whitelist.gametest;

import online.slavok.whitelist.SimpleWhitelist;
import online.slavok.whitelist.config.ConfigManager;
import online.slavok.whitelist.database.DatabaseManager;
//? if >=1.21 {
import net.fabricmc.fabric.api.gametest.v1.GameTest;
//?} else {
/*import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;*/
//?}
//? if >=1.22 {
/*import net.minecraft.gametest.framework.GameTestHelper;*/
//?} else {
import net.minecraft.test.TestContext;
//?}

/**
 * In-game smoke + functional test: boots a real headless server (so the mod's
 * onInitialize runs and the managers are live) and exercises the whitelist store
 * and config toggle end to end. Reaching the finish call proves the mod loaded on
 * this Minecraft version; the add/remove/toggle assertions (with negative
 * controls) prove the storage layer works in-server, not just in unit tests.
 *
 * The test body only touches this mod's own API, so only the GameTest harness
 * (interface / annotation / context type / finish call) is branched per version.
 */
//? if >=1.21 {
public class WhitelistGameTest {
//?} else {
/*public class WhitelistGameTest implements FabricGameTest {*/
//?}

    //? if >=1.21 {
    @GameTest
    //?} elif >=1.19 {
    /*@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)*/
    //?} else {
    /*@GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE)*/
    //?}
    //? if >=1.22 {
    /*public void whitelistRoundTrip(GameTestHelper context) {*/
    //?} else {
    public void whitelistRoundTrip(TestContext context) {
    //?}
        DatabaseManager db = SimpleWhitelist.INSTANCE.getDatabaseManager();
        String name = "GametestUser";
        db.removePlayer(name); // clean slate in case a prior run left it

        // negative control: the name is absent before we add it
        if (db.inWhitelist(name)) throw new RuntimeException("precondition: name should be absent");
        if (!db.addPlayer(name)) throw new RuntimeException("addPlayer returned false");
        // case-sensitive: the exact case is present, a different case is not
        if (!db.inWhitelist(name)) throw new RuntimeException("exact-case lookup failed");
        if (db.inWhitelist("gametestuser")) throw new RuntimeException("case-sensitivity failed: lowercase matched");
        // negative control: an unrelated name is still absent
        if (db.inWhitelist("SomeoneElse")) throw new RuntimeException("control: unrelated name present");
        if (!db.removePlayer(name)) throw new RuntimeException("removePlayer returned false");
        if (db.inWhitelist(name)) throw new RuntimeException("removed name still present");

        ConfigManager cfg = SimpleWhitelist.INSTANCE.getConfigManager();
        cfg.setWhitelist(false);
        if (cfg.getConfig().getWhitelist()) throw new RuntimeException("whitelist off failed");
        cfg.setWhitelist(true);
        if (!cfg.getConfig().getWhitelist()) throw new RuntimeException("whitelist on failed");

        //? if >=1.22 {
        /*context.succeed();*/
        //?} else {
        context.complete();
        //?}
    }
}
