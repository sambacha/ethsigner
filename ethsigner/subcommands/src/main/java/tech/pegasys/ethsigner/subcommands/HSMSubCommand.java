/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.ethsigner.subcommands;

import tech.pegasys.ethsigner.SignerSubCommand;
import tech.pegasys.signers.hsm.HSMConfig;
import tech.pegasys.signers.hsm.HSMWalletProvider;
import tech.pegasys.signers.secp256k1.api.Signer;
import tech.pegasys.signers.secp256k1.api.SignerProvider;
import tech.pegasys.signers.secp256k1.api.SingleSignerProvider;
import tech.pegasys.signers.secp256k1.common.SignerInitializationException;
import tech.pegasys.signers.secp256k1.hsm.HSMSignerFactory;

import java.nio.file.Path;

import com.google.common.base.MoreObjects;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/** HSM-based authentication related sub-command */
@Command(
    name = HSMSubCommand.COMMAND_NAME,
    description = "Sign transactions with a key stored in an HSM.",
    mixinStandardHelpOptions = true)
public class HSMSubCommand extends SignerSubCommand {

  // private static final String READ_PIN_FILE_ERROR = "Error when reading the pin from file.";
  public static final String COMMAND_NAME = "hsm-signer";

  public HSMSubCommand() {}

  @SuppressWarnings("unused") // Picocli injects reference to command spec
  @Spec
  private CommandLine.Model.CommandSpec spec;

  @Option(
      names = {"-l", "--library"},
      description = "The HSM PKCS11 library used to sign transactions.",
      paramLabel = "<LIBRARY_PATH>",
      required = true)
  private Path libraryPath;

  @Option(
      names = {"-s", "--slot-label"},
      description = "The HSM slot used to sign transactions.",
      paramLabel = "<SLOT_LABEL>",
      required = true)
  private String slotLabel;

  @Option(
      names = {"-p", "--slot-pin"},
      description = "The crypto user pin of the HSM slot used to sign transactions.",
      paramLabel = "<SLOT_PIN>",
      required = true)
  private String slotPin;

  @Option(
      names = {"-a", "--eth-address"},
      description = "Ethereum address of account to sign with.",
      paramLabel = "<ETH_ADDRESS>",
      required = true)
  private String ethAddress;

  private Signer createSigner() throws SignerInitializationException {
    final HSMConfig config =
        new HSMConfig(libraryPath != null ? libraryPath.toString() : null, slotLabel, slotPin);
    final HSMWalletProvider provider = new HSMWalletProvider(config);
    final HSMSignerFactory factory = new HSMSignerFactory(provider);
    return factory.createSigner(ethAddress);
  }

  @Override
  public SignerProvider createSignerFactory() throws SignerInitializationException {
    return new SingleSignerProvider(createSigner());
  }

  @Override
  public String getCommandName() {
    return COMMAND_NAME;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("library", libraryPath)
        .add("slot", slotLabel)
        .add("address", ethAddress)
        .toString();
  }
}
