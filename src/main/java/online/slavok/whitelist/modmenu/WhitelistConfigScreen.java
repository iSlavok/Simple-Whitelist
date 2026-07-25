//? if >=1.21 && <1.22 {
package online.slavok.whitelist.modmenu;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import online.slavok.whitelist.SimpleWhitelist;
import online.slavok.whitelist.config.ConfigManager;

/**
 * Minimal client config screen: a single toggle for the whitelist, useful when
 * the mod runs on a client that opens a world to LAN. Registered via ModMenu.
 */
public class WhitelistConfigScreen extends Screen {
    private final Screen parent;

    public WhitelistConfigScreen(Screen parent) {
        super(Text.literal("Simple Whitelist"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        this.addDrawableChild(ButtonWidget.builder(toggleText(), button -> {
            ConfigManager cfg = SimpleWhitelist.INSTANCE.getConfigManager();
            cfg.setWhitelist(!cfg.getConfig().getWhitelist());
            button.setMessage(toggleText());
        }).dimensions(centerX - 100, this.height / 2 - 12, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                .dimensions(centerX - 100, this.height / 2 + 12, 200, 20).build());
    }

    private Text toggleText() {
        boolean on = SimpleWhitelist.INSTANCE.getConfigManager().getConfig().getWhitelist();
        return Text.literal("Whitelist: " + (on ? "ON" : "OFF"));
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
//?}
