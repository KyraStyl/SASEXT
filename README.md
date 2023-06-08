# SASEXT Hybrid Complex Event Processing Engine

Complex Event Processing (CEP) is a mature technology providing particularly efficient solutions for pattern detection in streaming settings. Nevertheless, even the most advanced CEP engines struggle to deal with cases when the number of pattern matches grows exponentially, e.g., when the queries involve Kleene operators to detect trends. In this work, we present an overview of state-of-the-art CEP engines used for pattern detection, focusing also on systems that discover demanding event trends. The main contribution lies in the comparison of existing CEP engine alternatives and the proposal of a novel hash-endowed automata-based lazy hybrid execution engine, called SASEXT, that undertakes the processing of pattern queries involving Kleene patterns. Our proposal is orders of magnitude faster than existing solutions.

Conference paper: [link](https://dl.acm.org/doi/10.1145/3555776.3577734)
