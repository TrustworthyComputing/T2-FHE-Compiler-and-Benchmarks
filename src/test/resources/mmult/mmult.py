# Program to multiply two matrices using nested loops

r1 = r2 = c2 = 16
A = [i % 10 for i in range(r1 * r2)]
B = [(i + 4) % 10 for i in range(r1 * r2)]
result = [0] * (r1 * r2)
print("A: ", end="")
print(A)
print("B: ", end="")
print(B)
print(result)

# iterating by row of A
for i in range(r1):
  # iterating by column by B
  for j in range(c2):
    # iterating by rows of B
    for k in range(r2):
      result[i * r1 + j] += A[i*r2+k] * B[c2*k+j]

print("Result: ", end="")
print(result)
print("Max element: " + str(max(result)))
