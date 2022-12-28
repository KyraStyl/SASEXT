#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Feb 11 20:29:56 2022

@author: kyrastyl
"""

from random import randrange
import os
import sys, getopt
from pathlib import Path


man="""
This is a script to generate check data in a form of csv (comma separated)
Required parameters:
    -e, --events      Number of events to be created
    -o, --output      Name of the output file
Optional parameters:
    -p, --prob        The probability of equality of the next src to the previous dst
    -t, --transf      To transform an input file to comma separated
    -x, --extend      To extend with events an existing file
    -i, --input       The input file
Example:
    
    checkGen-transf.py -e 10000 -o dataset -c stream.conf
"""

index = 1
def transform(file):
    index = 1
    with open(file+".stream", 'r') as f:
        with open(file+"-transf.stream",'w') as w:
            for line in f:
                l = line.split()
                l[0] = str(index)
                l.insert(0, l[0])
                index+=1
                w.write(", ".join(l)+"\n")
                
def print_options(options,prob):
    print("\n***************************************************************")
    print("The generation of data begins!")
    print("Number of events to be created: ",options["events"])
    print("Output file: " + options["output"] + ".stream")
    print("Probability: ",prob)
    if "transf" in options:
        print("Input file to be transformed: ",options["input"])
    print("***************************************************************")
                
def produce(file, prob, extra):
    increaseprob = prob
    prevD = 1
    index = 0
    path = Path(os.path.dirname(os.path.abspath(__file__)))
    if(os.path.exists(os.path.join(path.parent.absolute(),file+".stream"))):
        index = sum(1 for _ in open(os.path.join(path.parent.absolute(),file+".stream")))
    with open(os.path.join(path.parent.absolute(),file+".stream"),'a') as f:
        for i in range(index+1, index+extra+1):
            prob = randrange(1,100)
            if(prob < 100-increaseprob+1):
                src = randrange(1,21)
                dest = randrange(1,21)
                while src == dest:
                    src = randrange(1,21)
                    dest = randrange(1,21)
                newEv = [str(i),str(i),"B"+str(src),"B"+str(dest)]
            else:
                nextD = randrange(1, 21)
                while nextD==prevD:
                    nextD = randrange(1,21)
                newEv = [str(i),str(i),"B"+str(prevD),"B"+str(nextD)]
                prevD = nextD
            newEv = ", ".join(newEv)
            f.write(newEv+"\n")
            
# reads input arguments
def get_options(argv):
    try:
        opts, args = getopt.getopt(argv,"he:o:p:t:x:i:",["events=","output=", "prob=","transf=", "extend=","input="])
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
            elif opt in ("-p","--prob"):
                data["prob"]=int(arg)
            elif opt in ("-t", "--transf"):
                data["transf"]= arg=='True'
            elif opt in ("-x","--extend"):
                data["extend"]= arg=='True'
            elif opt in ("-i","--input"):
                data["input"]=arg
        if "output" not in data or "events" not in data:
            print(man)
            sys.exit(3)
        if ("transf" in data or "extend" in data) and "input" not in data:
            print("Must include an input file!")
            print(man)
            sys.exit(4)
        return data
    except:
        print(man)
        sys.exit(5)

if __name__ == "__main__":
    options = get_options(sys.argv[1:])
    transf = False
    extend = True
    if "transf" in options:
        transf = options["transf"]
    if "extend" in options:
        extend = options["extend"]
    
    filename = options["output"]
    prob = 20
    if(options["prob"]):
        prob = options["prob"]
    
    print_options(options, prob)
    
    if transf:
        transform(filename)
        filename +="-transf"
    if extend:
        produce(filename, prob, options["events"])
        
