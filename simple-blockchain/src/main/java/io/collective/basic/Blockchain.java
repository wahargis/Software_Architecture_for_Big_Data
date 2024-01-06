package io.collective.basic;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Blockchain {

    // an ArrayList for holding our blocks
    private ArrayList<Block> blockchain = new ArrayList<Block>();

    public boolean isEmpty() {
        //Description:
        //   a method to determine if the blockchain is empty
        //   assumes that ArrayList.size return 0 for empty ArrayList
        //Parameters:
        //   N/A
        //Returns:
        //   boolean; True if nonzero blockchain size, False if blockchain size is zero
        return blockchain.size() == 0;
    }

    public void add(Block block) throws NoSuchAlgorithmException {
        //Description:
        //   a method for adding a block to our blockchain ArrayList
        //Parameters:
        //   block | Block | a class holding previousHash, creation timestamp, nonce, and the block's hash
        //Returns:
        //   void
        // Block newBlock = mine(block); // (tests assume blocks can be added if not mined) newBlock is added only after being successfully mined
        blockchain.add(block);
    }

    public int size() {
        //Description:
        //   a method for determining blockchain size
        //Parameters:
        //   N/A
        //Returns:
        //   integer of blockchain size based on blockchain ArrayList size
        return blockchain.size();
    }

    public boolean isValid() throws NoSuchAlgorithmException {
        //Description:
        //   a method for determining if the chain of blocks is valid
        //   assumes size() only returns nonnegative values
        //Parameters:
        //   N/A
        //Returns:
        //   boolean; True if all blocks in chain are valid, False if any blocks are invalid

        // check an empty chain
        if (this.size() == 0){
            return true; // an empty blockchain is valid
        }
        // check a chain of many
        for (int i = 0; i < blockchain.size(); i++){
            Block currentBlock = blockchain.get(i);           
            // check that current block has been mined
            if (!isMined(currentBlock)){
                return false;
            }
            // check the current block hash health
            String recalculatedHash = currentBlock.calculatedHash();
            if (!recalculatedHash.equals(currentBlock.getHash())){
                return false;
            }
            if (i > 0) {
                Block previousBlock = blockchain.get(i-1);
                // check that current block uses previous block's hash
                if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())){
                    return false;
                }
            }
        }
        return true;
    }

    /// Supporting functions that you'll need.

    public static Block mine(Block block) throws NoSuchAlgorithmException {

        Block mined = new Block(block.getPreviousHash(), block.getTimestamp(), block.getNonce());

        while (!isMined(mined)) {
            mined = new Block(mined.getPreviousHash(), mined.getTimestamp(), mined.getNonce() + 1);
        }
        return mined;
    }

    public static boolean isMined(Block minedBlock) {
        return minedBlock.getHash().startsWith("00");
    }
}