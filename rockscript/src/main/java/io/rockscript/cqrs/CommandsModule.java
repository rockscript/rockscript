package io.rockscript.cqrs;

import io.rockscript.gson.PolymorphicTypeAdapterFactory;

public interface CommandsModule {

  void registerCommands(PolymorphicTypeAdapterFactory polymorphicTypeAdapterFactory);
}
