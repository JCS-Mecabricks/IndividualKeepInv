//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package jcsmecabricks.individualkeepinv;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.datafixer.DataFixTypes;

public record ModPersistentStateType<T extends ModPersistentState>(String id, Supplier<T> constructor, Codec<T> codec, DataFixTypes dataFixType) {
    public boolean equals(Object o) {
        boolean var10000;
        if (o instanceof ModPersistentStateType<?> persistentStateType) {
            if (this.id.equals(persistentStateType.id)) {
                var10000 = true;
                return var10000;
            }
        }

        var10000 = false;
        return var10000;
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString() {
        return "SavedDataType[" + this.id + "]";
    }
}
