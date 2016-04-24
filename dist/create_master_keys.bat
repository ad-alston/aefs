java -jar AEFSKeygen.jar -new_keypair -num_bits 512 -params_path aefs_pp.key -msk_path aefs_msk.key
java -jar AEFSKeygen.jar -register_attributes master;server -params_path aefs_pp.key -msk_path aefs_msk.key
java -jar AEFSKeygen.jar -new_private_key master;server -key_path aefs_master.key -params_path aefs_pp.key -msk_path aefs_msk.key
java -jar RSAKeyManager.jar 2048 master