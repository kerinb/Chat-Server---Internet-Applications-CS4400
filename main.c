/*
***********************************     READ ME!!       *************************************************
    Date: 21/10/16
    Name:Breandan Kerin
    Number: 14310166

    This code implements basic Hashing and re(double) hashing.
    When you first run the code, you will get the results for the linear probing using the first function.
    For the double hashing using the first double hashing function
        comment in the index++ on line 107, and uncomment the   "index = Double_hash( test_strings[i], HASH_TABLE_SIZE_M);" on line 108
        That will give the results for double hashing  when you run the code.

    Next is to run the researched SDBM funtions.
        Linear probing: comment in lines 108 and  89
        uncomment lines 107 and 92
        double hashing: comment in lines 107 and uncomment lines 109

*/

#include <stdio.h> // printf() etc.
#include <string.h> // strcpy(), strlen() etc.
#include <math.h>

// i #define array lengths so i only have to change them in one place
#define NUM_TEST_KEYS 7
#define MAX_KEY_LENGTH 16
#define HASH_TABLE_SIZE_M 17

//Hash Table
char hash_table[HASH_TABLE_SIZE_M][MAX_KEY_LENGTH];

//Hash Function
int hash_function( const char *key, int table_size ) 
{
    int index = 0;
    index = (strlen(key))%table_size;

    return index;
}

//Double hash function
int Double_hash( const char *key, int table_size, int index )
 {
    int x = 123456789 + index;
    index = (strlen(key) + x)%table_size;

    return index;
}

//sdbm hash function ********* RESEARCHED*************************
int sdbm_hash(const char* key, int table_size)
{
    unsigned long hash = 0;
    int c;

    while(c = *key++)
        hash = c + (hash << 6) + (hash << 16) - hash;

    return hash%table_size;
}

// sdbm doublr hash function *************** RESEACRH**********************8
int sdbm_Dhash(const char* key, int table_size, int index){
    unsigned long hash = index + 123456789;
    int c;

    while(c = *key++)
        hash = c + (hash << 6) + (hash << 16) - hash;

    return hash%table_size;
}


int main() {
	// example: array of test strings to use as keys
	char test_strings[NUM_TEST_KEYS][MAX_KEY_LENGTH] = {
		"prince adam", "orko", "cringer", "teela", "aleet", "princess adora", "orko"
	};

	// example: store each key in the table and print the index for each test key
	printf("             key     table index    probe      total probe\n-----------------------------------------------------------\n" );
    int Totprobe = 0;

	for ( int i = 0; i < NUM_TEST_KEYS; i++ )
	{
        int probe =0;

        //The next call of the function is used for both linear probing and double hashing!
		int index = hash_function( test_strings[i], HASH_TABLE_SIZE_M );

		// calling sdbm function **********RESEACRH****************
        //int index = sdbm_hash(test_strings[i], HASH_TABLE_SIZE_M);

		Totprobe++;
		probe++;

        //filling hash table if nothing in index posistion
		if(hash_table[index][MAX_KEY_LENGTH] == NULL)
        {
            hash_table[index][MAX_KEY_LENGTH] = test_strings[i];
        }

        else
        {
            while (hash_table[index][MAX_KEY_LENGTH] != NULL)
            {
                index++;
                //index = Double_hash( test_strings[i], HASH_TABLE_SIZE_M, index) ;
                //int index = sdbm_Dhash(test_strings[i], HASH_TABLE_SIZE_M, index);
                probe++;
                Totprobe++;
            }

            hash_table[index][MAX_KEY_LENGTH] = test_strings[i];
        }

		// the %16s means print a string (%s) but pad it to 16 spaces
		printf( "%16s   %6i         %6i          % 6i\n", test_strings[i], index, probe, Totprobe);
	}

	return 0;
}
