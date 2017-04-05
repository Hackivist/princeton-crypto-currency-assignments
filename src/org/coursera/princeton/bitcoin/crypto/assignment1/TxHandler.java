package org.coursera.princeton.bitcoin.crypto.assignment1;

import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	private UTXOPool txHandlerPool;
    public TxHandler(UTXOPool utxoPool) {
        this.txHandlerPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	UTXOPool utxpool = new UTXOPool();
    	double intSum = 0;
    	double outSum = 0;
        //Verify inputs of Transactions
    	for(int i=0;i<tx.numInputs();i++){
    		Transaction.Input in = tx.getInput(i);
    		UTXO utxo = new UTXO(in.prevTxHash,in.outputIndex);
    		//(1) Check if output claimed(UTXO) is in current pool of UTXOs
    		if(!txHandlerPool.contains(utxo)){
    			return false;
    		}
    		//If yes, get output.
    		Transaction.Output out = txHandlerPool.getTxOutput(utxo);
    		//(2) Verify signature
    		if(!Crypto.verifySignature(out.address, tx.getRawDataToSign(i), in.signature)){
    			return false;
    		}
    		//(3) Check if UTXO is unique
    		if(utxpool.contains(utxo)){
    			return false;
    		}
    		//if not, add UTXO to unique pool
    		utxpool.addUTXO(utxo, out);
    		//track input sum value for (5)
    		intSum += out.value;
    	}
        for(Transaction.Output out: tx.getOutputs()){
        	//(4) check for non-negative output values
        	if(out.value<0){
        		return false;
        	}
        	//track output sum for (5)
        	outSum+=out.value;
        }
        //(5) check input sum is greater or equal to output sum
    	return intSum>=outSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	//Set to populate valid transactions
        Set<Transaction> validTxs = new HashSet<Transaction>();
    	for(Transaction tx: possibleTxs){
    		//check validity of current transaction and proceed
        	if(isValidTx(tx)){
        		//add to valid transaction set
        		validTxs.add(tx);
        		//update UTXO pool
        		for(Transaction.Input in: tx.getInputs()){
        			UTXO utxo = new UTXO(in.prevTxHash,in.outputIndex);
        			txHandlerPool.removeUTXO(utxo);
        		}
        		for(int i=0;i<tx.numOutputs();i++){
        			Transaction.Output out = tx.getOutput(i);
        			UTXO utxo = new UTXO(tx.getHash(),i);
        			txHandlerPool.addUTXO(utxo, out);
        		}
        	}
        }
    	//initialising result array
    	Transaction[] res = new Transaction[validTxs.size()];
    	//returns by adding set to result array
    	return validTxs.toArray(res);
    }
}