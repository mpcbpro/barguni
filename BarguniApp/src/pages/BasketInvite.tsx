import React, {useCallback, useEffect, useState} from 'react';
import {
  Alert,
  Image,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import {Snackbar} from 'react-native-paper';
import Clipboard from '@react-native-clipboard/clipboard';
import FontAwesome5 from 'react-native-vector-icons/FontAwesome5';
import {getBasketInfo} from '../api/basket';

function BasketInvite({route}) {
  const basketInfo = route.params;
  console.log(basketInfo, 'invite basket Info');
  const [inviteCode, setInviteCode] = useState('join code here');
  const [onCopied, setOnCopied] = useState(false);

  const init = useCallback(async () => {
    try {
      const res = await getBasketInfo(basketInfo.bkt_id);
      await setInviteCode(res.joinCode);
      console.log(res, '바스켓 조회');
    } catch (e) {
      console.log(e, 'ERROR IN BASKET INVITE');
    }
  }, [basketInfo.bkt_id]);
  useEffect(() => {
    init();
  }, [init]);

  const copyInviteCode = useCallback(() => {
    Clipboard.setString(inviteCode);
    // Alert.alert("초대코드가 복사되었습니다", "스낵바로 바꿔보기");
    setOnCopied(true);
  }, [inviteCode]);

  const onDismissSnackBar = useCallback(() => {
    setOnCopied(false);
  }, []);

  return (
    <View style={styles.container}>
      <View style={styles.titleContainer}>
        <Image
          source={require('../assets/basket_emoji.png')}
          style={styles.titleImg}
        />
        <Text style={{...styles.title, marginTop: 16}}>
          초대코드를 복사하여
        </Text>
        <Text style={{...styles.title, marginBottom: 20}}>
          친구를 초대해보세요!
        </Text>
      </View>
      <View style={styles.inviteContainer}>
        <TextInput
          value={inviteCode}
          editable={false}
          selectTextOnFocus={true}
          style={styles.inviteText}
        />
        <FontAwesome5 name="link" color={'#000'} style={styles.linkIcon} />
      </View>
      <Pressable onPress={copyInviteCode} style={styles.inviteBtn}>
        <Text style={styles.inviteBtnText}>초대코드 복사</Text>
      </Pressable>
      <Snackbar
        visible={onCopied}
        onDismiss={onDismissSnackBar}
        duration={3000}
        action={{
          label: '확인',
          onPress: () => {
            setOnCopied(false);
          },
        }}>
        초대코드가 복사되었습니다
      </Snackbar>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    justifyContent: 'center',
    alignItems: 'center',
  },
  titleImg: {
    width: 60,
    height: 60,
    resizeMode: 'cover',
  },
  titleContainer: {
    alignItems: 'center',
    textAlign: 'center',
  },
  title: {
    fontWeight: '900',
    fontSize: 22,
    color: '#000',
    textAlign: 'center',
    fontFamily: 'Pretendard-Light',
  },
  inviteContainer: {
    backgroundColor: '#F5F4F4',
    position: 'relative',
    borderRadius: 100,
  },
  inviteText: {
    color: '#000',
    paddingLeft: 20,
    paddingRight: 40,
    fontSize: 16,
    fontFamily: 'Pretendard-Light',
  },
  linkIcon: {
    position: 'absolute',
    top: '37%',
    right: 18,
  },
  inviteBtn: {
    backgroundColor: '#F5F4F4',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 100,
    marginTop: 28,
  },
  inviteBtnText: {
    color: '#0094FF',
    fontWeight: 'bold',
    fontSize: 12,
    fontFamily: 'Pretendard-Light',
  },
});

export default BasketInvite;
