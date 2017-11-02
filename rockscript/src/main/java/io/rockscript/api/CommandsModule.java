package io.rockscript.api;

import io.rockscript.gson.PolymorphicTypeAdapterFactory;

public interface CommandsModule {

  void registerCommands(PolymorphicTypeAdapterFactory polymorphicTypeAdapterFactory);
}
