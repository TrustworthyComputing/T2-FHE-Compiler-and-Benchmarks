#!/usr/bin/python

import matplotlib.pyplot as plt
import numpy as np

mydpi = 300
pltsize = (6, 1.5)

# Seconds
data = {
'Int $n = 50$': {
  'helib': 12,
  'lattigo': 37.7,
  'palisade': 19.9,
  'seal': 11.6 },
'FP $n = 50$': {
  'helib': 16,
  'lattigo': 251,
  'palisade': 175,
  'seal': 193 },
'Int $n = 100$': {
  'helib': 24,
  'lattigo': 75.3,
  'palisade': 40.4,
  'seal': 21.6 },
'FP $n = 100$': {
  'helib': 26.8,
  'lattigo': 502,
  'palisade': 372,
  'seal': 386 },
'Int $n = 150$': {
  'helib': 38.314,
  'lattigo': 107.401,
  'palisade': 56.891,
  'seal': 30.484 },
'FP $n = 150$': {
  'helib': 44.568,
  'lattigo': 756.000,
  'palisade': 578.062,
  'seal': 598.460 }
}

helib = []
lattigo = []
palisade = []
seal = []

x_axis_label = []
for k,val in data.items():
  x_axis_label.append(k)
  helib.append(val['helib'])
  lattigo.append(val['lattigo'])
  palisade.append(val['palisade'])
  seal.append(val['seal'])

N = len(palisade)
index = np.arange(N) # the x locations for the groups
width = 0.23 # the width of the bars

fig, ax = plt.subplots(figsize=pltsize)
ax.margins(0.02, 0.02)

rects1 = ax.bar(index - width, helib, width,
                color='xkcd:light salmon', hatch='//', edgecolor='black', linewidth=1)
rects2 = ax.bar(index, lattigo, width,
                color='#ffddbf', hatch='xx', edgecolor='black', linewidth=1)
rects3 = ax.bar(index + width, palisade, width,
                color='xkcd:ecru', hatch='--', edgecolor='black', linewidth=1)
rects4 = ax.bar(index + 2*width, seal, width,
                color='xkcd:very light green', hatch='..', edgecolor='black', linewidth=1)
ax.set_axisbelow(True)
ax.grid(True, axis='y', which="major", linewidth = "0.3", linestyle='--')
ax.set_yscale('log')
ax.set_ylim([10, 6000])
ax.set_yticks([10, 100, 1000])
ax.set_ylabel("Time (sec.)")
# ax.set_xlabel("Max Hidden Neurons")
ax.set_xticks(index + width / 2)
ax.set_xticklabels(x_axis_label)
ax.legend((rects1[0], rects2[0], rects3[0], rects4[0]),
          ("HElib", "Lattigo", "PALIS.", "SEAL"),
          fontsize=8, ncol=4, loc='upper left')

def autolabel_above(rects):
  for rect in rects:
    height = rect.get_height()
    if height <= 1:
      continue
    elif height < 10:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.1f' % (height), ha='center', va='bottom', fontsize=7)
    else:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.0f' % (height), ha='center', va='bottom', fontsize=7)

autolabel_above(rects1)
autolabel_above(rects2)
autolabel_above(rects3)
autolabel_above(rects4)

# plt.show()

plt.tight_layout()
plt.savefig("./nn.png", dpi=mydpi, bbox_inches="tight", pad_inches=0.03)
