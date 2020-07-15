import itertools

minsup = 0.01

cached_support = {}

data = []
for line in open('transactions.txt', 'r').readlines():
  transaction = list(map(int, line.split()))
  transaction.sort()
  data.append(transaction)

# Calculate support of an itemset
def support(A):
  n = 0
  setA = frozenset(A)
  if setA not in cached_support:
    for transaction in data:
      if set(A).issubset(set(transaction)):
        n += 1
    cached_support[setA] = (float)(n)/(float)(len(data))

  return cached_support[setA]

# Calculate support of two itemsets
def support2(X, Y):
  return support(set(X + Y))

# Returns the unique items of the dataset
def get_unique_items():
  unique_items = set()
  for transaction in data:
    unique_items = unique_items.union(transaction)
  return list(unique_items)

# Get itemsets of size 1 that fulfill the minimum support
def find_frequent_1_itemsets():
  result = []
  # Get all unique 1-item sets ...
  for i in get_unique_items():
    # ... and only keep those that meet the min-support
    sup = support([i])
    if (sup >= minsup):
      result.append([i])
      S.append([[i], sup])
  return result

# Algorithm
L = {} 
C = {}
S = [] # List of [[itemset, support value], ...]

L[1] = find_frequent_1_itemsets()
k = 2
while k-1 in L and len(L[k-1]) > 0:
  L[k] = []

  L_k1 = L[k-1]
  # Create a new set for every k-1-itemset ...
  for x in range(0, len(L_k1)):
    i_1 = L_k1[x]
    # ... with every k-1-itemset ...
    for y in range(0, len(L_k1)):
      i_2 = L_k1[y]

      # ... that only differs in the last item ...
      if i_1[0:-1] == i_2[0:-1] and i_1[-1] < i_2[-1]:
        c = i_1[:]
        c.append(i_2[-1])

        # ... and meets the support criteria
        sup = support(c)
        if sup >= minsup:
          L[k].append(c)
          S.append([c, sup])

  k += 1
  if k==5:
    break

# Print results
Co = {}

for s in S:
  if len(s[0]) not in Co:
    Co[len(s[0])] = 0
  Co[len(s[0])] = Co[len(s[0])] + 1
  print(str(s[0]) + " => " + str(s[1]))

for k,v in Co.items():
  print(str(k) + "-item => " + str(v))
