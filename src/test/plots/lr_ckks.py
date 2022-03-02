#!/usr/bin/python

import matplotlib.pyplot as plt
import numpy as np

mydpi = 300
pltsize = (6, 2)

# Milliseconds
data = {
'4': {
  'helib': 486,
  'lattigo': 75.5,
  'palisade': 153,
  'seal': 191 },
'8': {
  'helib': 843,
  'lattigo': 134.93,
  'palisade': 255,
  'seal': 339 },
'16': {
  'helib': 1577,
  'lattigo': 253.6,
  'palisade': 461,
  'seal': 645 }
}

helib = []
lattigo = []
palisade = []
seal = []

x_axis_label = []
for k,val in data.items():
  x_axis_label.append(k)
  helib.append(val['helib'] / 1000)
  lattigo.append(val['lattigo'] / 1000)
  palisade.append(val['palisade'] / 1000)
  seal.append(val['seal'] / 1000)

N = len(palisade)
index = np.arange(N) # the x locations for the groups
width = 0.22 # the width of the bars

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

ax.set_yscale('log')
ax.set_ylim([0.01, 10])
ax.set_ylabel("Time (sec.)")
ax.set_xlabel("Number of Attributes")
ax.set_xticks(index + width / 2)
ax.set_xticklabels(x_axis_label)
# ax.legend((rects1[0], rects2[0], rects3[0], rects4[0]),
#           ("HElib", "Lattigo", "PALISADE", "SEAL"),
#           fontsize=9, ncol=2, loc='upper left')

def autolabel_above(rects):
  for rect in rects:
    height = rect.get_height()
    if height <= 0.01:
      ax.text(rect.get_x() + rect.get_width()/2., 0.11, 'N/A', ha='center', va='bottom', fontsize=7)
      continue
    elif height < 10:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.2f' % (height), ha='center', va='bottom', fontsize=7)
    elif height < 1000:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.1f' % (height), ha='center', va='bottom', fontsize=7)
    else:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.0f' % (height), ha='center', va='bottom', fontsize=7)

autolabel_above(rects1)
autolabel_above(rects2)
autolabel_above(rects3)
autolabel_above(rects4)

# plt.show()

plt.tight_layout()
plt.savefig("./lr_ckks.png", dpi=mydpi, bbox_inches="tight", pad_inches=0.03)
