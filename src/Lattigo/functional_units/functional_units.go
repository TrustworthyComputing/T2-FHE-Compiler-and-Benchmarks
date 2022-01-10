package functional_units

import (
	"github.com/ldsec/lattigo/v2/bfv"
	"math"
)

var encryptorPk_ *bfv.Encryptor
var encoder_ *bfv.Encoder
var evaluator_ *bfv.Evaluator
var params_ *bfv.Parameters
var ptxt_mod_ int
var slots_ int

func FunitsInit(encryptorPk *bfv.Encryptor, encoder *bfv.Encoder,
	evaluator *bfv.Evaluator, params *bfv.Parameters,
	ptxt_mod, slots int) {
	encryptorPk_ = encryptorPk
	encoder_ = encoder
	evaluator_ = evaluator
	params_ = params
	ptxt_mod_ = ptxt_mod
	slots_ = slots
}

func EqPlain(ctxt *bfv.Ciphertext, ptxt *bfv.Plaintext) *bfv.Ciphertext {
	ctxt2 := (*encryptorPk_).EncryptNew(ptxt)
	return Eq(ctxt, ctxt2)
}

func Eq(c1, c2 *bfv.Ciphertext) *bfv.Ciphertext {
	num_squares := int(math.Log2(float64(ptxt_mod_ - 1)))
	tmp_ := c1.CopyNew()
	(*evaluator_).Sub(tmp_, c2, tmp_)
	tmp2_ := tmp_.CopyNew()
	for i := 0; i < num_squares; i++ { // square
		receiver := bfv.NewCiphertext(*params_, tmp2_.Degree()*2)
		(*evaluator_).Mul(tmp2_, tmp2_, receiver)
		tmp2_ = (*evaluator_).RelinearizeNew(receiver)
	}
	remaining := int((ptxt_mod_ - 1) - int(math.Pow(2.0, float64(num_squares))))
	for i := 0; i < remaining; i++ { // mult
		receiver := bfv.NewCiphertext(*params_, tmp2_.Degree()+tmp_.Degree())
		(*evaluator_).Mul(tmp2_, tmp_, receiver)
		tmp2_ = (*evaluator_).RelinearizeNew(receiver)
	}
	(*evaluator_).Neg(tmp2_, tmp2_)
	ptxt := bfv.NewPlaintext(*params_)
	ones := make([]uint64, slots_)
	for i := 0; i < slots_; i++ {
		ones[i] = 1
	}
	(*encoder_).EncodeUint(ones, ptxt)
	ones_ := (*encryptorPk_).EncryptNew(ptxt)
	(*evaluator_).Add(tmp2_, ones_, tmp2_)
	return tmp2_
}

func LtPlainRight(ctxt *bfv.Ciphertext, ptxt *bfv.Plaintext) *bfv.Ciphertext {
	ctxt2 := (*encryptorPk_).EncryptNew(ptxt)
	return Lt(ctxt, ctxt2)
}

func LtPlainLeft(ptxt *bfv.Plaintext, ctxt *bfv.Ciphertext) *bfv.Ciphertext {
	ctxt1 := (*encryptorPk_).EncryptNew(ptxt)
	return Lt(ctxt1, ctxt)
}

func Lt(c1, c2 *bfv.Ciphertext) *bfv.Ciphertext {
	num_squares := int(math.Log2(float64(ptxt_mod_ - 1)))
	ptxt := bfv.NewPlaintext(*params_)
	arr := make([]uint64, slots_)
	for i := 0; i < slots_; i++ {
		arr[i] = 0
	}
	(*encoder_).EncodeUint(arr, ptxt)
	result_ := (*encryptorPk_).EncryptNew(ptxt)

	start := -(ptxt_mod_ - 1) / 2
	for i := start; i < 0; i++ { // square
		tmp_ := c1.CopyNew()
		(*evaluator_).Sub(tmp_, c2, tmp_)

		for j := 0; j < slots_; j++ {
			arr[j] = uint64(-i)
		}
		(*encoder_).EncodeUint(arr, ptxt)
		i_ := (*encryptorPk_).EncryptNew(ptxt)
		(*evaluator_).Add(tmp_, i_, tmp_)
		tmp2_ := tmp_.CopyNew()

		for j := 0; j < num_squares; j++ { // square
			receiver := bfv.NewCiphertext(*params_, tmp2_.Degree()*2)
			(*evaluator_).Mul(tmp2_, tmp2_, receiver)
			tmp2_ = (*evaluator_).RelinearizeNew(receiver)
		}
		remaining := int((ptxt_mod_ - 1) - int(math.Pow(2.0, float64(num_squares))))
		for j := 0; j < remaining; j++ { // mult
			receiver := bfv.NewCiphertext(*params_, tmp2_.Degree()+tmp_.Degree())
			(*evaluator_).Mul(tmp2_, tmp_, receiver)
			tmp2_ = (*evaluator_).RelinearizeNew(receiver)
		}
		tmp_ = tmp2_.CopyNew()
		(*evaluator_).Neg(tmp_, tmp_)
		for j := 0; j < slots_; j++ {
			arr[j] = 1
		}
		(*encoder_).EncodeUint(arr, ptxt)
		ones_ := (*encryptorPk_).EncryptNew(ptxt)
		(*evaluator_).Add(tmp_, ones_, tmp_)
		(*evaluator_).Add(result_, tmp_, result_)
	}
	return result_
}

func LeqPlainRight(ctxt *bfv.Ciphertext, ptxt *bfv.Plaintext) *bfv.Ciphertext {
	ctxt2 := (*encryptorPk_).EncryptNew(ptxt)
	return Leq(ctxt, ctxt2)
}

func LeqPlainLeft(ptxt *bfv.Plaintext, ctxt *bfv.Ciphertext) *bfv.Ciphertext {
	ctxt1 := (*encryptorPk_).EncryptNew(ptxt)
	return Leq(ctxt1, ctxt)
}

func Leq(c1, c2 *bfv.Ciphertext) *bfv.Ciphertext {
	less_ := Lt(c1, c2)
	equal_ := Eq(c1, c2)
	tmp_ := less_.CopyNew()
	receiver := bfv.NewCiphertext(*params_, tmp_.Degree()+equal_.Degree())
	(*evaluator_).Mul(tmp_, equal_, receiver)
	tmp_ = (*evaluator_).RelinearizeNew(receiver)
	result_ := less_.CopyNew()
	(*evaluator_).Add(result_, equal_, result_)
	(*evaluator_).Sub(result_, tmp_, result_)
	return result_
}
