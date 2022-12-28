#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Jan 21 18:26:05 2022

@author: kyrastyl
"""


import sys, getopt
import re
from random import randrange, seed


man="""
This is a script to generate stock data in a form of csv (comma separated)
Required parameters:
    -e, --events      Number of events to be created
    -o, --output      Name of the output file
Optional parameters:
    -c, --config      The configuration file
    
Example:
    
    stockGen.py -e 10000 -o dataset -c stream.conf
"""

class Config():
    maxPrice = 100
    numOfSymbols = 10
    maxVolume = 1000
    randomSeed = 52
    increaseProb = 0
  
    def __init__(self, *args):
        if(len(args)>0):
            self.__init1__(args[0], args[1], args[2], args[3], args[4])

    def __init1__(self, maxPrice, numOfSymbols, maxVolume, randomSeed, increaseProb):
        self.maxPrice = maxPrice
        self.numOfSymbols = numOfSymbols
        self.maxVolume = maxVolume
        self.randomSeed = randomSeed
        self.increaseProb = increaseProb

    def __str__(self):
        string = " --maxPrice = "+ str(self.maxPrice)+"\n"
        string += " --numOfSymbols = "+ str(self.numOfSymbols)+"\n"
        string += " --maxVolume = "+ str(self.maxVolume)+"\n"
        string += " --randomSeed = "+ str(self.randomSeed)+"\n"
        string += " --increaseProb = "+ str(self.increaseProb)
        return string

class StockEvent():
    price = ""
    volume = ""
    eid = -1
    timestamp = -1
    symbol = -1
    
    def __init__(self, eid, timestamp, symbol, price, volume):
        self.eid = eid
        self.timestamp = timestamp
        self.symbol = symbol
        self.price = price
        self.volume = volume
    
    def __str__(self):
        return str(self.timestamp)+", "+str(self.eid)+", "+str(self.symbol)+", "+str(self.price)+", "+str(self.volume)+"\n"

# reads input arguments
def get_options(argv):
    try:
        opts, args = getopt.getopt(argv,"he:c:o:",["events=","config=","output="])
    except:
        print(man)
        sys.exit(2)
    try:
        data={}
        for opt,arg in opts:
            if opt == '-h':
                print(man)
                sys.exit()
            elif opt in ("-e", "--events"):
                data["events"]=int(arg)
            elif opt in ("-o", "--output"):
                data["output"]=arg
            elif opt in ("-c", "--config"):
                data["config"]=arg
        if "output" not in data or "events" not in data:
            print(man)
            sys.exit(3)
        return data
    except:
        print(man)
        sys.exit(4)


def read_conf(conf_file):
    maxPrice, numOfSymbols, maxVolume, randomSeed, increaseProb = 0,0,0,0,0
    with open(conf_file, 'r') as f:
        for line in f:
            tokens = re.split("= ", line)
            if("maxPrice" in tokens[0]):
                maxPrice = int(tokens[1])
            elif("numOfSymbol" in tokens[0]):
                numOfSymbols = tokens[1]
            elif("maxVolume" in tokens[0]):
                maxVolume = tokens[1]
            elif("randomSeed" in tokens[0]):
                randomSeed = tokens[1]
            elif("increaseProb" in tokens[0]):
                increaseProb = tokens[1]
    return Config(maxPrice, numOfSymbols, maxVolume, randomSeed, increaseProb)
            

# Prints info and configuration
def print_options(options, config):
    print("\n***************************************************************")
    print("The generation of data begins!")
    print("Number of events to be created: ",options["events"])
    print("Output file: " + options["output"] + ".stream")
    if "config" in options: print("Configuration file:",options["config"])
    print(config.__str__())
    print("***************************************************************")
    
def generate_data(size, conf):
    data = []
    price = 1
    seed(Config.randomSeed)         
   
    for i in range(1,size+1):
        symbol = randrange(0,int(conf.numOfSymbols))
        rangeSelect = randrange(0,100) 
        
        if(rangeSelect < 100-int(conf.increaseProb)+1):
            price = randrange(1,price+1)
        else:
            price = randrange(price, int(conf.maxPrice)+1)
        
        volume = randrange(1, int(conf.maxVolume)+1)
        data.append(StockEvent(i, i, symbol, price, volume))
    return data




































        
