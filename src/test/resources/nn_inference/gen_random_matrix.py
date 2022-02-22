from random import randint
def main():
  l = 10
  w = 2
  lo = 0
  hi = 256
  mat = [[] for i in range(l)]
  for i in range(l):
    for j in range(w):
      mat[i].append(randint(lo, hi))
      print(mat[i][j],end=", ")
    print()

main()
  
