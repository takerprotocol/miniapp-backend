package com.abmatrix.bool.tg.contracts;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * bool 链质押合约
 *
 * @author PeterWong
 * @date 2024年7月31日
 */
public class Mining extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_EXITSERVICE = "exitService";

    public static final String FUNC_JOINSERVICE = "joinService";

    public static final String FUNC_REMOVEDEVICE = "removeDevice";

    public static final String FUNC_REMOVEDEVICEVOTERS = "removeDeviceVoters";

    public static final String FUNC_UPDATEDEVICEALLOWNEWVOTES = "updateDeviceAllowNewVotes";

    public static final String FUNC_UPDATEDEVICECOMMISSION = "updateDeviceCommission";

    public static final String FUNC_UPDATEVOTES = "updateVotes";

    public static final String FUNC_CHALLENGES = "challenges";

    public static final String FUNC_WORKINGDEVICES = "workingDevices";

    @Deprecated
    protected Mining(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
                     BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Mining(String contractAddress, Web3j web3j, Credentials credentials,
                     ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Mining(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice,
                     BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Mining(String contractAddress, Web3j web3j, TransactionManager transactionManager,
                     ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> exitService(byte[] id) {
        final Function function = new Function(FUNC_EXITSERVICE, Arrays.<Type>asList(new Bytes32(id)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> joinService(byte[] id) {
        final Function function = new Function(FUNC_JOINSERVICE, Arrays.<Type>asList(new Bytes32(id)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> removeDevice(byte[] id, Boolean force) {
        final Function function = new Function(FUNC_REMOVEDEVICE,
                Arrays.<Type>asList(new Bytes32(id), new org.web3j.abi.datatypes.Bool(force)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> removeDeviceVoters(byte[] id, List<String> voters) {
        final Function function = new Function(FUNC_REMOVEDEVICEVOTERS,
                Arrays.<Type>asList(new Bytes32(id),
                        new DynamicArray<org.web3j.abi.datatypes.Address>(org.web3j.abi.datatypes.Address.class,
                                org.web3j.abi.Utils.typeMap(voters, org.web3j.abi.datatypes.Address.class))),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> updateDeviceAllowNewVotes(byte[] id, Boolean allow) {
        final Function function = new Function(FUNC_UPDATEDEVICEALLOWNEWVOTES,
                Arrays.<Type>asList(new Bytes32(id), new org.web3j.abi.datatypes.Bool(allow)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> updateDeviceCommission(byte[] id, BigInteger new_commission) {
        final Function function = new Function(FUNC_UPDATEDEVICECOMMISSION,
                Arrays.<Type>asList(new Bytes32(id), new Uint256(new_commission)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> updateVotes(List<byte[]> ids, List<BigInteger> amounts) {
        final Function function = new Function(FUNC_UPDATEVOTES,
                Arrays.<Type>asList(
                        new DynamicArray<Bytes32>(Bytes32.class, org.web3j.abi.Utils.typeMap(ids, Bytes32.class)),
                        new DynamicArray<Uint256>(Uint256.class, org.web3j.abi.Utils.typeMap(amounts, Uint256.class))),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> challenges(BigInteger session) {
        final Function function = new Function(FUNC_CHALLENGES, Arrays.<Type>asList(new Uint256(session)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<List> workingDevices(BigInteger session) {
        final Function function = new Function(FUNC_WORKINGDEVICES, Arrays.<Type>asList(new Uint256(session)),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {
                }));
        return new RemoteFunctionCall<List>(function, new Callable<List>() {
            @Override
            @SuppressWarnings("unchecked")
            public List call() throws Exception {
                List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                return convertToNative(result);
            }
        });
    }

    @Deprecated
    public static Mining load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
                              BigInteger gasLimit) {
        return new Mining(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Mining load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
                              BigInteger gasPrice, BigInteger gasLimit) {
        return new Mining(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Mining load(String contractAddress, Web3j web3j, Credentials credentials,
                              ContractGasProvider contractGasProvider) {
        return new Mining(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Mining load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
                              ContractGasProvider contractGasProvider) {
        return new Mining(contractAddress, web3j, transactionManager, contractGasProvider);
    }
}