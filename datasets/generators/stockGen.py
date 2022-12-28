#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Jan 21 11:55:30 2022

@author: kyrastyl
"""



from utility_functions import *
from pathlib import Path
import os
   


def main():
    options = get_options(sys.argv[1:]) #read options and load to a dictinary
    data=[]
    size=options["events"]
    Configuration = None
    if "config" in options:
        Configuration = read_conf(options["config"])
    else:
        Configuration = Config()
    data = generate_data(size, Configuration)
    print_options(options, Configuration)
    
    #write to file
    path = Path(os.path.dirname(os.path.abspath(__file__)))
    with open(os.path.join(path.parent.absolute(), options["output"]+".stream"),"w") as f:
        for index, e in enumerate(data):
            f.write(e.__str__())
        

if __name__ == "__main__":
    main()
