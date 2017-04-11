f = open('batch.txt', 'w')

for i in range(20000):
    f.write("SET A" + str(i) + " " + str(i) + "\n")
    f.write("GET A" + str(i) + "\n")

f.close()
