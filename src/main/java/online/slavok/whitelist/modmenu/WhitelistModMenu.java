//? if >=1.21 && <1.22 {
package online.slavok.whitelist.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class WhitelistModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return WhitelistConfigScreen::new;
    }
}
//?}
