<h1>PairwiseDistanceCalculator</h1>
**Class**: `maltcms.commands.fragments.cluster.PairwiseDistanceCalculator`  
**Workflow Slot**: STATISTICS  
**Description**: Calculates pairwise distances/similarities between time series.  

---

<h2>Variables</h2>
<h3>Required</h3>

<h3>Optional</h3>

<h3>Provided</h3>
	var.minimizing_array_comp
	var.pairwise_distance_matrix
	var.pairwise_distance_names


---

<h2>Configurable Properties</h2>
**Name**: `minArrayComp`  
**Default Value**: `minimizing_array_comp`  
**Description**:  
Sets the variable name used to store whether the similarity or distance function used required minimization (1) or maximization (0)  

---

**Name**: `minimizingLocalDistance`  
**Default Value**: `false`  
**Description**:  
If true, assumes that the comparison function between mass spectra behaves like a cost function (smaller is better). If false, assumes that the function behaves like a similarity or score function (larger is better).  

---

**Name**: `pairsWithFirstElement`  
**Default Value**: `false`  
**Description**:  
If true, calculates similarities / distances only with the first chromatogram. If false, calculates all (n-1)*n/2 alignments.  

---

**Name**: `pwdExtension`  
**Default Value**: ``  
**Description**:  
Suffix for the pairwise_distances.cdf files. E.g. for "_CUST", the result file will be named "pairwise_distances_CUST.cdf" .  

---

**Name**: `workerFactory`  
**Default Value**: `MziDtwWorkerFactory(bandWidthPercentage=0.25, globalGapPenalty=1.0, extension=, globalBand=false, minScansBetweenAnchors=10, precalculatePairwiseDistances=false, saveLayoutImage=false, useAnchors=false, numberOfEICsToSelect=0, useSparseArrays=false, anchorRadius=0, similarity=DtwTimePenalizedPairwiseSimilarity(expansionWeight=1.0, matchWeight=1.0, compressionWeight=1.0, retentionTimeSimilarity=GaussianDifferenceSimilarity(tolerance=5.0, threshold=0.0), denseMassSpectraSimilarity=ArrayCos{}), saveDtwMatrix=false, savePairwiseSimilarityMatrix=false, normalizeAlignmentValue=false)`  
**Description**:  
The worker factory to use for chromatogram / MS comparison. Use either TicDtwWorkerFactory or MziDtwWorkerFactory.  

---


