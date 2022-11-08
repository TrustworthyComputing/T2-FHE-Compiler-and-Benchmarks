#!/usr/bin/python

import matplotlib.pyplot as plt
import numpy as np

mydpi = 300
pltsize = (6, 2.0)

data = {
  'SEAL BFV'  : {
    'e3': 18.5,
    'eva': 0,
    't2': 18.6,
    'tenseal': 18.4,
  },
  'SEAL CKKS'  : {
    'e3': 0,
    'eva': 5.6,
    't2': 10.7,
    'tenseal': 9.5,
  },
  'PALISADE BFV' : {
    'e3': 11.1,
    'eva': 0,
    't2': 10.5,
    'tenseal': 0,
  },
}

e3 = []
eva = []
t2 = []
tenseal = []

x_axis_label = []
for k,val in data.items():
  x_axis_label.append(k)
  e3.append(val['e3'])
  eva.append(val['eva'])
  t2.append(val['t2'])
  tenseal.append(val['tenseal'])

N = len(e3)
index = np.arange(N) # the x locations for the groups
width = 0.22 # the width of the bars

fig, ax = plt.subplots(figsize=pltsize)
ax.margins(0.02, 0.02)

rects1 = ax.bar(index - width, t2, width,
                color='xkcd:light salmon', hatch='//', edgecolor='black', linewidth=1)
rects2 = ax.bar(index, e3, width,
                color='xkcd:ecru', hatch='xx', edgecolor='black', linewidth=1)
rects3 = ax.bar(index + width, eva, width,
                color='xkcd:very light green', hatch='--', edgecolor='black', linewidth=1)
rects4 = ax.bar(index + 2*width, tenseal, width,
                color='xkcd:very light blue', hatch='\\\\', edgecolor='black', linewidth=1)

ax.set_axisbelow(True)
ax.grid(True, axis='y', which="major", linewidth = "0.3", linestyle='--')
ax.set_yscale('log')
ax.set_ylim([1, 100])
ax.set_yticks([1, 10, 100])
ax.set_ylabel("Time (sec.)")
ax.set_xlabel("HE Library")
ax.set_xticks(index + width / 2)
ax.set_xticklabels(x_axis_label)
ax.legend((rects1[0], rects2[0], rects3[0], rects4[0]),
          ("T2", "E3", "EVA", "TenSEAL"),
          fontsize=9, ncol=5, loc='upper center')

def autolabel_above(rects, lib_color):
  for rect in rects:
    height = rect.get_height()
    if height <= 0.014:
      ax.text(rect.get_x() + rect.get_width()/2., 2, 'N/S', color='black', bbox=dict(facecolor='none', color=lib_color, linewidth=5, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
      continue
    if height > 10:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.1f' % (height), ha='center', va='bottom', fontsize=7)
    else:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.2f' % (height), ha='center', va='bottom', fontsize=7)

autolabel_above(rects1, 'xkcd:light salmon')
autolabel_above(rects2, 'xkcd:ecru')
autolabel_above(rects3, 'xkcd:very light green')
autolabel_above(rects4, 'xkcd:very light blue')

# plt.show()

plt.tight_layout()
plt.savefig("./mmult_compilers.png", dpi=mydpi, bbox_inches="tight", pad_inches=0.03)
