import sys,json,time
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from verify_notifications_local import call,create,wait_notice,root
accounts=json.loads((root/'target/local-run/notification-test-accounts.json').read_text())
owner=call('POST','/authentication/sign-in',{'email':accounts['ownerEmail'],'password':'LocalNoticeTest2026!'})
alert,_,_=create(owner)
(root/'target/local-run/notification-recovery-alert.json').write_text(json.dumps({'alertId':alert['id']}))
print('Created during Communities outage:',alert['id'],flush=True)
time.sleep(4)
